package sasa.progression.sasaEnhancedProgression.techtree;

import java.util.HashMap;

public class TechnologyRequirementBundle {

    public final int parts;
    public final HashMap<String, RequirementInformationBundle> requirements = new HashMap<>();

    public TechnologyRequirementBundle(int parts) {
        this.parts = parts;
    }

    public record RequirementInformationBundle(int amount, boolean scaling) {}

}
