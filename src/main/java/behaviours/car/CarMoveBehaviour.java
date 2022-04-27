package behaviours.car;

import agents.CarAgent;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import messages.StringMessages;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.util.Set;

public class CarMoveBehaviour extends Behaviour {

    private final CarAgent carAgent;

    private final CarFSMBehaviour fsm;

    private GraphPath<Point, DefaultWeightedEdge> path;

    private int currentPathIndex;

    private boolean done;

    public CarMoveBehaviour(CarFSMBehaviour fsm) {
        super(fsm.getAgent());

        this.fsm = fsm;

        this.currentPathIndex = 0;
        this.carAgent = (CarAgent) fsm.getAgent();
        this.done = false;
    }

    @Override
    public void onStart() {
        // path until first of currentPath
        this.path = GraphUtils.getPathFromAtoB(this.carAgent.getGraph(), this.carAgent.getCurrentLocation().getName(), this.fsm.getCurrentPath().getVertexList().get(0).getName());
    }

    @Override
    public int onEnd() {
        System.out.printf("%s: CarMoveBehaviour: onEnd()\n", this.carAgent.getLocalName());
        this.reset();
        return super.onEnd();
    }

    @Override
    public void action() {
        if (this.currentPathIndex < this.path.getVertexList().size() - 1) {
            this.currentPathIndex++;
            this.carAgent.moveTo(this.path.getVertexList().get(this.currentPathIndex));
        } else {
            // send message to the human stating that the car has arrived
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            Set<DFAgentDescription> agents = ServiceUtils.search(this.carAgent, ServiceUtils.buildRideName(this.fsm.getCurrentHuman().getLocalName()));
            // add all agents as receivers
            agents.forEach(agent -> msg.addReceiver(agent.getName()));
            msg.setContent(StringMessages.CAR_ARRIVED);
            this.myAgent.send(msg);
            this.done = true;
        }
    }

    @Override
    public boolean done() {
        return this.done;
    }

    @Override
    public void reset() {
        super.reset();
        this.done = false;
    }
}
