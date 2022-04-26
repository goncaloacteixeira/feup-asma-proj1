package behaviours.car;

import behaviours.human.AskCarRideBehaviour;
import graph.vertex.Point;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import messages.CarRideCFPMessage;
import utils.ServiceUtils;

import java.io.IOException;
import java.util.Set;
import java.util.Vector;

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
            cfp.setContentObject(new CarRideCFPMessage(this.start, this.end));
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
        // TODO
        System.out.println("got " + responses.size() + " responses");
        for (Object o : responses) {
            ACLMessage response = (ACLMessage) o;
            ACLMessage reply = response.createReply();
            // if acceptances is empty
            if (acceptances.isEmpty()) {
                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            } else {
                reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            }
            acceptances.addElement(reply); //
        }

        this.askCarRideBehaviour.setDone(true);
    }

    @Override
    protected void handleAllResultNotifications(Vector notifications) {
        // TODO
        System.out.println("got " + notifications.size() + " result notifications");
    }
}
