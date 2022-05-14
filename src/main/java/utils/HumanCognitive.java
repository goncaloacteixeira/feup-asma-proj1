package utils;

import messages.CarRideProposeMessage;

import java.util.Set;

/**
 * Set of methods that enable the human to make decisions.
 */
public interface HumanCognitive {

    /**
     * Given a set of car ride proposal.
     *
     * @param proposals the set of car ride proposals
     * @return the best car ride proposal, or null if there are no suitable car ride proposals
     */
    static CarRideProposeMessage decideCarRide(Set<CarRideProposeMessage> proposals) {
        // TODO maybe do this more cleverly
        CarRideProposeMessage bestProposal = null;
        // the best proposal is the smallest one
        float bestProposalCost = Float.MAX_VALUE;
        for (CarRideProposeMessage proposal : proposals) {
            float value = proposal.getPrice();
            if (value < bestProposalCost) {
                bestProposal = proposal;
                bestProposalCost = value;
            }
        }
        return bestProposal;
    }
}
