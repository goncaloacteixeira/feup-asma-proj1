package behaviours.car;

import behaviours.human.AskCarRideBehaviour;
import graph.vertex.Point;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import messages.CarRideCFPMessage;
import messages.CarRideProposeMessage;
import utils.HumanCognitive;
import utils.ServiceUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

// TODO negotiate price with cars
public class CarRideContractNetInitiatorBehaviour extends ContractNetInitiator {

    private final Point start;

    private final Point end;

    private final AskCarRideBehaviour askCarRideBehaviour;

    public CarRideContractNetInitiatorBehaviour(AskCarRideBehaviour askCarRideBehaviour, Agent a, ACLMessage cfp, Point start, Point end) {
        super(a, cfp);

        this.askCarRideBehaviour = askCarRideBehaviour;
        this.start = start;
        this.end = end;
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
            cfp.setContentObject(new CarRideCFPMessage(this.start, this.end)); // TODO include number of passengers
        } catch (IOException e) {
            e.printStackTrace();
            return v;
        }

        // get available cars in service
        Set<DFAgentDescription> cars = ServiceUtils.search(this.myAgent, ServiceUtils.CAR_RIDE);
        System.out.printf("%s: found %d cars in service\n", this.myAgent.getLocalName(), cars.size());

        cars.forEach(car -> cfp.addReceiver(car.getName()));

        v.addElement(cfp);
        return v;
    }

    @Override
    protected void handleAllResponses(Vector responses, Vector acceptances) {
        System.out.printf("%s: got %d responses\n", this.myAgent.getLocalName(), responses.size());

        try {
            // gets the proposal inside the messages
            Set<CarRideProposeMessage> proposals = this.getProposals(responses);

            // sends proposals to human cognitive to decide
            CarRideProposeMessage bestPropose = HumanCognitive.decideCarRide(proposals);

            for (Object o : responses) {
                ACLMessage response = (ACLMessage) o;
                ACLMessage reply = response.createReply();
                // accepts the best proposal and rejects the rest
                if (((CarRideProposeMessage) response.getContentObject()).equals(bestPropose)) {
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                } else {
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }
                acceptances.addElement(reply);
            }
        } catch (UnreadableException e) {
            System.out.printf("%s: could not read propose message, aborting.\n", this.myAgent.getLocalName());
            return;
        }

        this.askCarRideBehaviour.setDone(true);
    }

    @Override
    protected void handleAllResultNotifications(Vector notifications) {
        // TODO
        System.out.println("got " + notifications.size() + " result notifications");
    }

    private Set<CarRideProposeMessage> getProposals(Vector responses) throws UnreadableException {
        Set<CarRideProposeMessage> proposals = new HashSet<>();
        for (Object response : responses) {
            ACLMessage message = (ACLMessage) response;
            CarRideProposeMessage propose = (CarRideProposeMessage) message.getContentObject();
            proposals.add(propose);
        }

        return proposals;
    }
}
