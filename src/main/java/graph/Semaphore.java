package graph;

public class Semaphore extends Point {
    public Semaphore(String name) {
        super(name);
    }

    @Override
    public String getColor() {
        return Colorable.ROAD_COLOR;
    }
}
