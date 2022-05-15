package utils;

/**
 * Set of methods that enable the cars to make decisions.
 */
public interface CarCognitive {

    /**
     * Returns a possible price for a ride, given the cost of it
     *
     * @param pathCost the cost of the ride
     * @return the possible price for the ride
     */
    static float getRidePrice(double pathCost) {
        // get random number between 1 and 2
        float random = (float) (Math.random() * 2) + 1;

        // return the price
        return (float) (pathCost * random);
    }

    /**
     * Returns a boolean that states if a price should be accepted, given a path cost
     *
     * @param pathCost   the cost of the ride
     * @param askedPrice the price that the car is asking for
     * @return true if the price is accepted, false otherwise
     */
    static boolean shouldAcceptRide(double pathCost, float askedPrice) {
        // TODO maybe do this better, its always a random number but it can be more specific as negotiations are made
        if (askedPrice <= pathCost) {
            return false;
        }

        // accept with 75% chance
        return Math.random() < 0.5;
    }

    static float getBetterRidePrice(double pathCost, float askedPrice) {
        if (askedPrice < pathCost) {
            return askedPrice;
        }

        // get random number between pathCost and askedPrice
        return (float) ((float) (Math.random() * (askedPrice - pathCost)) + pathCost);
    }
}
