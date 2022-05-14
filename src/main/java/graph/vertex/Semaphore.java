package graph.vertex;

import graph.Colorable;

import java.io.Serializable;

public class Semaphore extends Point implements Serializable {
    public Semaphore(String name) {
        super(name);
    }

    @Override
    public String getColor() {
        return Colorable.ROAD_COLOR;
    }
}
