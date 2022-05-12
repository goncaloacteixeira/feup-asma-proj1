package messages;

import graph.vertex.Point;
import lombok.Getter;

import java.io.Serializable;

/**
 * A blind request of a ride from human to car.
 * It's blind because the human is not offering any price, it is up for the car to decide.
 *
 * @param start the start point of the ride
 * @param end the end point of the ride
 */
public record CarRideCFPBlindRequestMessage(@Getter Point start, @Getter Point end) implements Serializable {}
