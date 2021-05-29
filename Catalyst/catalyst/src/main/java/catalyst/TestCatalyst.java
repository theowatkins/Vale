package catalyst;

import java.io.*;
import java.util.*;
import java.util.function.Function;

import org.json.simple.*;
import org.json.simple.parser.*;


public class TestCatalyst {
    private static final String MISSING_NODES = "Did not find all expected AST nodes in revised .vast file";
    private static final String MISSING_KL = "Expected knownLive to be true, but knownLive was false";

    private static ArrayList<JSONObject> recursiveCollect(JSONObject astNode, Function<JSONObject, Boolean> checkFields) {
        ArrayList<JSONObject> returnList = new ArrayList<JSONObject>();
        if (checkFields.apply(astNode)) {
            returnList.add(astNode);
        }

        // iterate fields
        for (Iterator iterator = astNode.keySet().iterator(); iterator.hasNext(); ) {
            String field = (String) iterator.next();
            Object value = astNode.get(field);
            if (value instanceof JSONObject) {
                returnList.addAll(recursiveCollect((JSONObject)value, checkFields));
            } else if (value instanceof JSONArray) {
                for (Object arrayVal : (JSONArray)value) {
                    assert !(arrayVal instanceof JSONArray) : "Unexpected type of JSONArray entry (JSONArray)";
                    if (arrayVal instanceof JSONObject) {
                        returnList.addAll(recursiveCollect((JSONObject)arrayVal, checkFields));
                    }
                }
            } 
        }
        
        return returnList;
    }

    private static void fuel_kl(String outFile, int numChecks) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject ast = (JSONObject)parser.parse(new FileReader(outFile));
            ArrayList<JSONObject> memberLoads = recursiveCollect(ast, (node) -> { 
                return ((String)node.get("__type")).equals("MemberLoad") && ((String)node.get("memberName")).equals("fuel__ut_0"); });
            
            assert memberLoads.size() >= numChecks : MISSING_NODES;
            int checks = 0;
            for (int i = 0; i < memberLoads.size(); i++) {
                if ((boolean)memberLoads.get(i).get("structKnownLive"))
                    checks += 1;
            }
            assert checks == numChecks : MISSING_KL;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void fuel_not_kl(String outFile, int numChecks) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject ast = (JSONObject)parser.parse(new FileReader(outFile));
            ArrayList<JSONObject> memberLoads = recursiveCollect(ast, (node) -> { 
                return ((String)node.get("__type")).equals("MemberLoad") && ((String)node.get("memberName")).equals("fuel__ut_0"); });
            
            assert memberLoads.size() >= numChecks : MISSING_NODES;
            int checks = 0;
            for (int i = 0; i < memberLoads.size(); i++) {
                if (!((boolean)memberLoads.get(i).get("structKnownLive")))
                    checks += 1;
            }
            assert checks == numChecks : MISSING_KL;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void engine_kl(String outFile, int numChecks) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject ast = (JSONObject)parser.parse(new FileReader(outFile));
            ArrayList<JSONObject> memberLoads = recursiveCollect(ast, (node) -> { 
                return ((String)node.get("__type")).equals("MemberLoad") && ((String)node.get("memberName")).equals("engine_0"); });
            
            assert memberLoads.size() >= numChecks : MISSING_NODES;
            int checks = 0;
            for (int i = 0; i < memberLoads.size(); i++) {
                if ((boolean)memberLoads.get(i).get("structKnownLive"))
                    checks += 1;
            }
            assert checks == numChecks : MISSING_KL;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static void engine_not_kl(String outFile, int numChecks) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject ast = (JSONObject)parser.parse(new FileReader(outFile));
            ArrayList<JSONObject> memberLoads = recursiveCollect(ast, (node) -> { 
                return ((String)node.get("__type")).equals("MemberLoad") && ((String)node.get("memberName")).equals("engine_0"); });
            
            assert memberLoads.size() >= numChecks : MISSING_NODES;
            int checks = 0;
            for (int i = 0; i < memberLoads.size(); i++) {
                if (!((boolean)memberLoads.get(i).get("structKnownLive")))
                    checks += 1;
            }
            assert checks == numChecks : MISSING_KL;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void runTests() {
        //TODO: get paths in a portable manner
        String[] args1 = {
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test1\\out\\build.vast", 
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test1\\out\\speedy-build.vast" };
        String[] args2 = {
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test2\\out\\build.vast", 
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test2\\out\\speedy-build.vast" };
        String[] args3 = {
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test3\\out\\build.vast", 
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test3\\out\\speedy-build.vast" };
        String[] args4 = {
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test4\\out\\build.vast", 
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test4\\out\\speedy-build.vast" };
        String[] args5 = {
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test5\\out\\build.vast", 
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test5\\out\\speedy-build.vast" };
        String[] args6 = {
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test6\\out\\build.vast", 
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test6\\out\\speedy-build.vast" };
        String[] args7 = {
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test7\\out\\build.vast", 
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test7\\out\\speedy-build.vast" };
        String[] args8 = {
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test8\\out\\build.vast", 
            "C:\\Users\\theow\\Desktop\\Thesis\\Catalyst\\testing\\test8\\out\\speedy-build.vast" };
            
        Catalyst.main(args1);
        fuel_kl(args1[1], 1);
        Catalyst.main(args2);
        fuel_kl(args2[1], 1);
        Catalyst.main(args3);
        fuel_kl(args3[1], 1);
        Catalyst.main(args4);
        engine_kl(args4[1], 1);
        fuel_not_kl(args4[1], 1);
        Catalyst.main(args5);
        fuel_kl(args5[1], 4);
        Catalyst.main(args6);
        fuel_kl(args6[1], 1);
        Catalyst.main(args7);
        fuel_not_kl(args7[1], 1);
        fuel_kl(args7[1], 1);
        Catalyst.main(args8);
        engine_not_kl(args8[1], 2);
        fuel_kl(args8[1], 2);


        System.out.println("Tests completed successfully!");
    }
    
}


