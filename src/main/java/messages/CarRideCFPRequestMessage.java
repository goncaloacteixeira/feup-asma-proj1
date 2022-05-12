package messages;

import graph.vertex.Point;
import lombok.Getter;

import java.io.Serializable;

/**
 * A proposal of a ride from human to car.
 *
 * @param start the start point of the ride
 * @param end the end point of the ride
 * @param price the price of the ride
 */
public record CarRideCFPRequestMessage(@Getter Point start, @Getter Point end, @Getter float price) implements Serializable {}
