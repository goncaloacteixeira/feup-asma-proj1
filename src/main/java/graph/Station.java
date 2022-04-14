package graph;

public class Station extends Point {
    public Station(String name) {
        super(name);
    }

    @Override
    public String getColor() {
        return Colorable.SUBWAY_COLOR;
    }
}
