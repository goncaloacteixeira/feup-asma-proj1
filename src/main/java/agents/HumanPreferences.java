package agents;

public class HumanPreferences {
    protected double roadWeight = 0;
    protected double streetWeight = 0;
    protected double subwayWeight = 0;
    protected boolean carShareInitiator = false;

    public HumanPreferences noRoads() {
        this.roadWeight = Double.MAX_VALUE;
        return this;
    }

    public HumanPreferences noStreets() {
        this.streetWeight = Double.MAX_VALUE;
        return this;
    }

    public HumanPreferences carShareInitiator() {
        this.carShareInitiator = true;
        return this;
    }

    public HumanPreferences carShareInitiator(boolean flag) {
        this.carShareInitiator = flag;
        return this;
    }

    public HumanPreferences noSubway() {
        this.subwayWeight = Double.MAX_VALUE;
        return this;
    }

    public double getRoadWeight() {
        return roadWeight;
    }

    public double getStreetWeight() {
        return streetWeight;
    }

    public double getSubwayWeight() {
        return subwayWeight;
    }

    public boolean isCarShareInitiator() {
        return carShareInitiator;
    }
}
