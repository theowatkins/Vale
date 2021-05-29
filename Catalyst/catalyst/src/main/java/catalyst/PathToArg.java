package catalyst;

import java.util.*;

public class PathToArg {
    public ArrayList<Integer> Path;
    public OptionalLong ArgIdx;

    public PathToArg(ArrayList<Integer> p, OptionalLong a) {
        this.Path = p;
        this.ArgIdx = a;
    }
}