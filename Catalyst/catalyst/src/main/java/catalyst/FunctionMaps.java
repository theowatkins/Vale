// FunctionMaps.java
package catalyst;

import java.lang.StackWalker.Option;
import java.util.*;

public class FunctionMaps {

    private static class LivenessInfo {
        public Boolean Liveness;
        public StructMember[] Members;
        public OptionalLong Parent;

        public LivenessInfo(StructMember[] members, Boolean liveness) {
            this.Liveness = liveness;
            this.Members = members;
            this.Parent = OptionalLong.empty();
        }
    }

    private static class Variable {
        public OptionalLong ObjectId;
        public Ref Info;

        public Variable(OptionalLong id, Ref info) {
            this.ObjectId = id;
            this.Info = info;
        }
    }

    public String FunctionName;

    // Args and some objects aren't stackified and are identified by this 
    // unique decrementing number starting from -1
    public Long ObjCounter;

    public ReturnInfo Returns;
    
    // Stores objects references
    private HashMap<Long, LivenessInfo> Objects = new HashMap<Long, LivenessInfo>();

    // Stores variables references
    private HashMap<Long, Variable> Variables = new HashMap<Long, Variable>();

    public FunctionMaps(String funName) {
        this.FunctionName = funName;
        this.ObjCounter = Long.valueOf(0);
    }

    public Long addObject(StructMember[] members, Boolean liveness, Boolean isArg) {
        Objects.put(ObjCounter, new LivenessInfo(members, liveness));
        for (StructMember m : members) {
            if (m.Id.isPresent() && (m.Ownership.equals("Own") || isArg)) {
                assert Objects.containsKey(m.Id.getAsLong()) : "Member not found";
                Objects.get(m.Id.getAsLong()).Parent = OptionalLong.of(ObjCounter);
            }
        }
        ObjCounter++;
        return ObjCounter - 1;
    }

    public void addVariable(Long id, Ref info, OptionalLong objectId) {
        Variables.put(id, new Variable(objectId, info));
    }

    public void swapMembers(OptionalLong obj, StructMember[] newMembers) {
        if (obj.isPresent() && Objects.containsKey(obj.getAsLong())) {
            Objects.get(obj.getAsLong()).Members = newMembers;
        }
    }

    public void killRef(Long refId) {
        if (Variables.containsKey(refId)) {
            Variables.remove(refId);
        }
    }

    public Boolean isAlive(OptionalLong objId) {
        if (objId.isPresent() && Objects.containsKey(objId.getAsLong())) {
            return Objects.get(objId.getAsLong()).Liveness;
        }

        return false;
    }

    public OptionalLong getObject(Long refId) {
        if (Variables.containsKey(refId)) {
            Variable var = Variables.get(refId);
            return var.ObjectId;
        }
        return OptionalLong.empty();
    }

    public OptionalLong getMemberObj(OptionalLong parentId, Long memberIdx) {
        if (parentId.isPresent() && Objects.containsKey(parentId.getAsLong())) {
            LivenessInfo parent = Objects.get(parentId.getAsLong());
            if (parent.Members.length > memberIdx) {
                return parent.Members[memberIdx.intValue()].Id;
            }
        }
        return OptionalLong.empty();
    }

    public OptionalLong[] getMemberIds(OptionalLong parentId) {
        if (parentId.isPresent() && Objects.containsKey(parentId.getAsLong())) {
            LivenessInfo parent = Objects.get(parentId.getAsLong());
            OptionalLong[] ids = new OptionalLong[parent.Members.length];
            for (int i=0; i<parent.Members.length; i++) {
                ids[i] = parent.Members[i].Id;
            }
            return ids;
        }
        return new OptionalLong[0];
    }

    public void setMember(OptionalLong objId, Long memberIndex, OptionalLong newMember) {
        if (objId.isPresent() && Objects.containsKey(objId.getAsLong())) {
            LivenessInfo object = Objects.get(objId.getAsLong());
            if (object.Members.length > memberIndex) {
                OptionalLong oldMember = object.Members[memberIndex.intValue()].Id;
                if (oldMember.isPresent()) {
                    killObj(oldMember);
                }
                object.Members[memberIndex.intValue()].Id = newMember;
            } 
        }
    }

    public void setReturnInfo(OptionalLong objId, Long argId, Optional<String> structName, String ownership) {
        StructMember[] members = new StructMember[0];

        PathToArg retObjAsArg = getPathToArg(objId);
        HashMap<Long, MemberArgMap> memberMap = new HashMap<Long, MemberArgMap>();

        if (objId.isPresent()) {
            members = Objects.get(objId.getAsLong()).Members;
        }

        //returned object placed in map first with key of -1
        memberMap.put(Long.valueOf(-1), new MemberArgMap(members, this));
        fillMemberMap(memberMap, members, argId);

        Returns = new ReturnInfo(structName, ownership, retObjAsArg, memberMap);
    }

    private void fillMemberMap(HashMap<Long, MemberArgMap> memberMap, 
                                StructMember[] members, Long argId) {
        for (StructMember m : members) {
            if (m.Id.isPresent()){
                LivenessInfo memberInfo = Objects.get(m.Id.getAsLong());
                StructMember[] memberMembers = memberInfo.Members;
                memberMap.put(m.Id.getAsLong(), new MemberArgMap(memberMembers, this));
                fillMemberMap(memberMap, memberMembers, argId);
            }
        }
    }

    // Inputs: ID of object that is no longer guaranteed to be alive
    // Outputs: None
    public void killObj(OptionalLong obj) {
        if (obj.isPresent()) {
            LivenessInfo objLivenessInfo = Objects.get(obj.getAsLong());
            objLivenessInfo.Liveness = false;
            // Any owned members are no longer knownLive
            for (StructMember m : objLivenessInfo.Members) {
                if (m.Id.isPresent() && m.Ownership.equals("Own")) {
                    killObj(m.Id);
                }
            }
        }
    }

    public OptionalLong[] getArgObjs() {
        // Create array with length of argument list
        Long c = Long.valueOf(-1);
        while(Variables.containsKey(c)) {
            c--;
        }
        int size = -1 * (c.intValue() + 1);
        OptionalLong[] argObjs = new OptionalLong[size];

        // Get the object referenced by each argument
        for (int i = 0; i < size; i++) {
            Long idx = Long.valueOf(-1 * (i + 1));
            OptionalLong obj = getObject(idx);

            argObjs[i] = obj;
        }

        return argObjs;
    }

    public PathToArg getPathToArg(OptionalLong obj) {
        OptionalLong[] argObjs = getArgObjs();
        OptionalLong arg = OptionalLong.empty();
        OptionalLong child = OptionalLong.empty();
        OptionalLong curObj = obj;
        ArrayList<Integer> path = new ArrayList<Integer>();

        while (curObj.isPresent()) {
            assert Objects.containsKey(curObj.getAsLong()) : "obj not found";
            LivenessInfo curInfo = Objects.get(curObj.getAsLong());

            // Update path 
            if (child.isPresent()) {
                for (int i = 0; i < curInfo.Members.length; i++) {
                    if (child.equals(curInfo.Members[i].Id)) {
                        // add index of child to path
                        path.add(i);
                        break;
                    }
                }
            }
            Long argIdx = Long.valueOf(0);
            // Check if current object is argument
            for (OptionalLong argObj : argObjs) {
                if (argObj.isPresent() && argObj.equals(curObj)) {
                    arg = OptionalLong.of(argIdx);
                }
                argIdx++;
            }
            
            if (arg.isPresent()) break;

            child = curObj;
            curObj = curInfo.Parent;
        }

        Collections.reverse(path);

        return new PathToArg(path, arg);
    }

    public void killVaryingMembers(OptionalLong obj) {
        if (obj.isPresent()) {
            LivenessInfo objLivenessInfo = Objects.get(obj.getAsLong());
            int memCount = 0;
            // Any varying members are no longer knownLive
            for (StructMember m : objLivenessInfo.Members) {
                if (m.Id.isPresent()) {
                    if (m.Variability.equals("Varying")) {
                        if (m.Ownership.equals("Own")) {
                            killObj(m.Id);
                            killVaryingMembers(m.Id);
                        } else {
                            // create new NOT knownlive object for varying borrow refs to point to
                            LivenessInfo info = Objects.get(m.Id.getAsLong());
                            Long newObj = addObject(info.Members, false, false);
                            objLivenessInfo.Members[memCount] = new StructMember(OptionalLong.of(newObj), m.Ownership, m.Variability);
                            killVaryingMembers(OptionalLong.of(newObj));
                        }
                    }
                }
                memCount++;
            }
        }
    }

    public void printMaps() {
        System.out.println("Function: " + FunctionName);
        this.printObjects();
        this.printVariables();
    }

    public void printObjects() {
        System.out.println("Objects: ");
        for (HashMap.Entry<Long, LivenessInfo> entry : Objects.entrySet()) {
            System.out.print("\t" + entry.getKey() + ": " + Boolean.toString(entry.getValue().Liveness));
            if (entry.getValue().Members != null && entry.getValue().Members.length > 0) {
                System.out.print(", Members: [");
                for (StructMember mem : entry.getValue().Members) {
                    if (mem.Id.isPresent()) {
                        System.out.print(mem.Id.getAsLong());
                        System.out.print(" ");
                        System.out.print(mem.Ownership);
                    } else {
                        System.out.print("const");
                    }
                    System.out.print(", ");
                }
                System.out.println("]");
            }
        }
        System.out.println();
    }

    public void printVariables() {
        System.out.println("Variables: ");
        for (HashMap.Entry<Long, Variable> entry : Variables.entrySet()) {
            System.out.println("\t" + entry.getKey() + ": " + entry.getValue().ObjectId);
        }
        System.out.println();
    }
}