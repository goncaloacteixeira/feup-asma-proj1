package agents;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HumanResults implements Serializable {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String path;

    @Getter
    @Setter
    private double originalCost;

    @Getter
    @Setter
    private double finalCost;

    @Getter
    @Setter
    private boolean initiator;

    private int nSharedSegments = 0;

    private int nCarServiceFares = 0;

    private final List<SharedSegment> sharedSegments = new ArrayList<>();

    private final List<CarServiceFare> carServiceFares = new ArrayList<>();

    public HumanResults(String name, String path, double originalCost, boolean initiator) {
        this.name = name;
        this.path = path;
        this.originalCost = originalCost;
        this.initiator = initiator;
    }

    public void addSharedSegment(SharedSegment sharedSegment) {
        sharedSegments.add(sharedSegment);
        nSharedSegments++;
    }

    public void addCarServiceFare(CarServiceFare carServiceFare) {
        carServiceFares.add(carServiceFare);
        nCarServiceFares++;
    }


    public String[] valuesToWrite() {
        return new String[]{
                name,
                path,
                String.valueOf(initiator),
                String.valueOf(originalCost),
                String.valueOf(finalCost),
                sharedSegments.toString(),
                carServiceFares.toString(),
                String.valueOf(nSharedSegments),
                String.valueOf(nCarServiceFares)
        };
    }
}
