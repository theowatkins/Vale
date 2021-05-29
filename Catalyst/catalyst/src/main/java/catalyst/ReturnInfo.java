package catalyst;

import java.util.*;

public class ReturnInfo {
    public Optional<String> StructName;
    public String Ownership;
    public PathToArg RetArgIdx;
    public HashMap<Long, MemberArgMap> MemberMap; 

    public ReturnInfo(Optional<String> sn, String own, PathToArg rai, HashMap<Long, MemberArgMap> mai) {
        this.StructName = sn;
        this.Ownership = own;
        this.RetArgIdx = rai;
        this.MemberMap = mai;
    }
}