package behaviours.human;

import behaviours.car.CarRideContractNetInitiatorBehaviour;
import graph.GraphUtils;
import graph.exceptions.NoRoadsException;
import graph.vertex.Point;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import lombok.Setter;

/**
 * This behaviour is used to ask a car to ride.
 * It starts the negotiation of a contract net with the car for the ride.
 */
public class AskCarRideBehaviour extends Behaviour{

    private final FSMHumanBehaviour fsm;

    @Setter
    private boolean done;

    public AskCarRideBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        super(fsmHumanBehaviour.getAgent());
        this.fsm = fsmHumanBehaviour;
        this.done = false;
    }

    @Override
    public void onStart() {
        try {
            Point p1 = fsm.path.getVertexList().get(fsm.currentLocationIndex);
            Point p2 = GraphUtils.roadStop(fsm.graph, fsm.path, fsm.currentLocationIndex);
            System.out.printf("%s: Asking for a car ride\n", getAgent().getLocalName());
            this.myAgent.addBehaviour(new CarRideContractNetInitiatorBehaviour(this, this.myAgent, new ACLMessage(ACLMessage.CFP), p1, p2));
        } catch (NoRoadsException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void action() {
    }

    @Override
    public boolean done() {
        return this.done;
    }
}
