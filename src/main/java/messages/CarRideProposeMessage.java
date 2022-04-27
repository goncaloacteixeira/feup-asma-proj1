package messages;

import lombok.Getter;

import java.io.Serializable;

/**
 * Content of a message that holds a proposal for a car ride, given by the car to the human.
 *
 * @param price    the price of the ride
 * @param distance the distance of the ride
 * @param capacity the capacity of the car
 * @param carName  the name of the car
 */
public record CarRideProposeMessage(@Getter float price, @Getter float distance, @Getter int capacity,
                                    @Getter String carName) implements Serializable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CarRideProposeMessage that = (CarRideProposeMessage) o;
        return Float.compare(that.price, price) == 0 && carName.equals(that.carName);
    }
}
