package catalyst;

import java.util.OptionalLong;

public class StructMember {
    OptionalLong Id;
    String Ownership;
    String Variability;

    public StructMember(OptionalLong id, String ownership, String variability) {
        this.Id = id;
        this.Ownership = ownership;
        this.Variability = variability;
    }
}