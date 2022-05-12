package behaviours.car;

import behaviours.human.AskCarRideBehaviour;
import graph.vertex.Point;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import messages.CarRideCFPBlindRequestMessage;
import messages.CarRideCFPRequestMessage;
import messages.CarRideProposeMessage;
import utils.HumanCognitive;
import utils.ServiceUtils;

import java.io.IOException;
import java.util.*;

public class CarRideContractNetInitiatorBehaviour extends ContractNetInitiator {

    private final Point start;

    private final Point end;

    private final AskCarRideBehaviour askCarRideBehaviour;

    private final Optional<Float> priceOptional;

    /**
     * Try all cars except the one that is here.
     * This is for when we are in an auction, the car in here is the one with the best price, so we just ask the others.
     */
    private final Optional<String> exceptCarNameOptional;

    public CarRideContractNetInitiatorBehaviour(AskCarRideBehaviour askCarRideBehaviour, Agent a, ACLMessage cfp, Point start, Point end) {
        super(a, cfp);

        this.askCarRideBehaviour = askCarRideBehaviour;
        this.start = start;
        this.end = end;

        // if there is no predefined price
        this.priceOptional = Optional.empty();
        this.exceptCarNameOptional = Optional.empty();
    }

    public CarRideContractNetInitiatorBehaviour(AskCarRideBehaviour askCarRideBehaviour, Agent a, ACLMessage cfp, Point start, Point end, float price, String exceptedCar) {
        super(a, cfp);

        this.askCarRideBehaviour = askCarRideBehaviour;
        this.start = start;
        this.end = end;

        this.priceOptional = Optional.of(price);
        this.exceptCarNameOptional = Optional.of(exceptedCar);
    }

    @Override
    public int onEnd() {
        this.reset();
        return super.onEnd();
    }

    @Override
    protected Vector<ACLMessage> prepareCfps(ACLMessage cfp) {
        Vector<ACLMessage> v = new Vector<>();

        try {
            if (this.priceOptional.isEmpty()) {
                // if there is no price sends blind request
                cfp.setContentObject(new CarRideCFPBlindRequestMessage(this.start, this.end));
            } else {
                // if there is a price sends a message with the price
                cfp.setContentObject(new CarRideCFPRequestMessage(this.start, this.end, this.priceOptional.get())); // TODO include number of passengers
            }
        } catch (IOException e) {
            // won't happen
            e.printStackTrace();
            return v;
        }

        // get available cars in service
        Set<DFAgentDescription> cars = ServiceUtils.search(this.myAgent, ServiceUtils.CAR_RIDE);
        var totalCars = new ArrayList<String>();
        cars.forEach(car -> {
            if (this.exceptCarNameOptional.isEmpty() || !this.exceptCarNameOptional.get().equals(car.getName().getName())) {
                // if there is no car to except or the car is not the one to except
                cfp.addReceiver(car.getName());
                totalCars.add(car.getName().getLocalName());
            }
        });
        System.out.printf("%s: sending to %d cars\n", this.myAgent.getLocalName(), totalCars.size());

        v.addElement(cfp);
        return v;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        System.out.printf("%s: got %d responses\n", this.myAgent.getLocalName(), responses.size());

        try {
            // gets the proposal inside the messages
            Set<ACLMessage> realResponses = this.getRealResponses(responses);

            // if there are no better proposals
            if (realResponses.isEmpty()) {
                // this should only run if it is not the first iteration and there is already a saved reply
                // we accept the saved proposal
                this.askCarRideBehaviour.confirmBestProposal();

                System.out.printf("%s: no better proposals, accepting saved proposal\n", this.myAgent.getLocalName());
                return;
            }

            // if there are better proposals

            // sends proposals to human cognitive to decide
            Set<CarRideProposeMessage> proposals = this.getProposals(realResponses);
            CarRideProposeMessage bestProposal = HumanCognitive.decideCarRide(proposals);

            // if there is a better proposal, reject the last one
            if (bestProposal != null) {
                this.askCarRideBehaviour.rejectBestProposal();
            }

            for (ACLMessage response : realResponses) {
                ACLMessage reply = response.createReply();
                if (response.getContentObject().equals(bestProposal)) {
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                } else {
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }
                acceptances.addElement(reply);
            }

            this.askCarRideBehaviour.setBestProposal(bestProposal);
        } catch (UnreadableException e) {
            System.out.printf("%s: could not read propose message, aborting.\n", this.myAgent.getLocalName());
            throw new RuntimeException(e);
        }

        System.out.println(acceptances.size());
    }

    @Override
    protected void handleAllResultNotifications(Vector notifications) {
        // TODO
        System.out.printf("%s: got %d notifications\n", this.myAgent.getLocalName(), notifications.size());
    }

    private Set<ACLMessage> getRealResponses(Vector responses) throws UnreadableException {
        Set<ACLMessage> realResponses = new HashSet<>();
        for (Object response : responses) {
            ACLMessage message = (ACLMessage) response;
            // if message is of type propose
            if (message.getPerformative() == ACLMessage.PROPOSE) {
                realResponses.add(message);
            }
        }

        return realResponses;
    }

    private Set<CarRideProposeMessage> getProposals(Set<ACLMessage> realResponses) {
        Set<CarRideProposeMessage> proposals = new HashSet<>();
        for (ACLMessage message : realResponses) {
            try {
                proposals.add((CarRideProposeMessage) message.getContentObject());
            } catch (UnreadableException e) {
                System.out.printf("%s: could not read propose message, aborting.\n", this.myAgent.getLocalName());
                throw new RuntimeException(e);
            }
        }
        return proposals;
    }
}
