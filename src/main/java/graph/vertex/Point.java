package graph.vertex;

import graph.Colorable;

import java.io.Serializable;
import java.util.Objects;

public class Point implements Colorable, Serializable {
    private final String name;

    public Point(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Point point = (Point) o;
        return Objects.equals(name, point.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }

    public static Point instance(String name) {
        return new Point(name) {
            @Override
            public boolean equals(Object o) {
                return super.equals(o);
            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public String toString() {
                return super.toString();
            }
        };
    }

    public String getName() {
        return name;
    }

    @Override
    public String getColor() {
        return Colorable.STREET_COLOR;
    }
}
