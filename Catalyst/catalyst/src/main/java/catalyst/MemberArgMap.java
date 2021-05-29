package catalyst;

import java.util.*;

public class MemberArgMap {
    public StructMember[] members;
    public PathToArg[] membersAsArgs;

    public MemberArgMap(StructMember[] members, FunctionMaps fmaps) {
        this.members = members;

        PathToArg[] memsAsArgs = new PathToArg[members.length];
        int c = 0;
        for (StructMember m : members) {
            memsAsArgs[c] = fmaps.getPathToArg(m.Id);
            c++;
        }
        this.membersAsArgs = memsAsArgs;
    }
}