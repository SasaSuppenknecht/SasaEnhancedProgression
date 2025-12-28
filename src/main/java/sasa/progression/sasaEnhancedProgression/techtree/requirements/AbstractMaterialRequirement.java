package sasa.progression.sasaEnhancedProgression.techtree.requirements;

public class AbstractMaterialRequirement {
    protected final int needed;
    protected int given;

    public AbstractMaterialRequirement(int needed) {
        this.needed = needed;
        this.given = 0;
    }

    public int getGiven() {
        return given;
    }

    public void increaseGiven(int additional) {
        given += additional;
        if (given > needed) {
            given = needed;
        }
    }

    public int getNeeded() {
        return needed;
    }

    public boolean isFulfilled() {
        return given >= needed;
    }
}
