package agents;

import java.io.Serializable;

public record CarServiceFare(String path, double fare, double expectedCost) implements Serializable {
    @Override
    public String toString() {
        return String.format("Path: %s, Fare: %.02f, Expected: %.02f", path, fare, expectedCost);
    }
}
