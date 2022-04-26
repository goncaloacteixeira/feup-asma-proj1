package behaviours.human;

import graph.GraphUtils;
import graph.RoadPathPoints;
import graph.exceptions.NoRoadsException;
import graph.vertex.Point;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

class CNIHelperBehaviour extends Behaviour {
    private final FSMHumanBehaviour fsmHumanBehaviour;
    private final String service;
    private final Pair<String, Boolean> done = Pair.of("done", false);
    private boolean busy = false;

    /**
     * Behaviour to create a new ContractNet Initiator behaviour
     *
     * @param fsmHumanBehaviour parent behaviour
     */
    public CNIHelperBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        super(fsmHumanBehaviour.getAgent());
        this.fsmHumanBehaviour = fsmHumanBehaviour;
        this.service = FSMHumanBehaviour.CAR_SHARE_RESP_SERVICE;
    }

    @Override
    public void onStart() {
        try {
            Point p1 = fsmHumanBehaviour.path.getVertexList().get(fsmHumanBehaviour.currentLocationIndex);
            Point p2 = GraphUtils.roadStop(fsmHumanBehaviour.graph, fsmHumanBehaviour.path, fsmHumanBehaviour.currentLocationIndex);
            System.out.printf("%s: Announcing Car Share from %s to %s\n", myAgent.getLocalName(), p1, p2);
        } catch (NoRoadsException e) {
            throw new RuntimeException(e);
        }

        super.onStart();
    }

    @Override
    public boolean done() {
        return done.getSecond();
    }

    @Override
    public int onEnd() {
        busy = false;
        done.setSecond(false);
        return super.onEnd();
    }

    /**
     * This behaviour is busy waiting until the contract net initiator behavior ends
     */
    @Override
    public void action() {
        if (!busy) {
            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            // Deadline is 10s after message is sent
            cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            cfp.setContent("dummy-action");

            Set<DFAgentDescription> agents = ServiceUtils.search(myAgent, service);

            agents.forEach(agent -> cfp.addReceiver(agent.getName()));

            try {
                Point p1 = fsmHumanBehaviour.path.getVertexList().get(fsmHumanBehaviour.currentLocationIndex);
                Point p2 = GraphUtils.roadStop(fsmHumanBehaviour.graph, fsmHumanBehaviour.path, fsmHumanBehaviour.currentLocationIndex);
                GraphPath<Point, DefaultWeightedEdge> roadPath = GraphUtils.getPathFromAtoB(fsmHumanBehaviour.graph, p1.getName(), p2.getName());

                cfp.setContentObject(new RoadPathPoints(p1.getName(), p2.getName()));

                Behaviour behaviour = new CarShareContractNetInitiator(myAgent, cfp, agents.size(), done, roadPath, fsmHumanBehaviour.graph);
                busy = true;
                myAgent.addBehaviour(behaviour);
            } catch (NoRoadsException | IOException e) {
                done.setSecond(Boolean.TRUE);
            }
        }
    }
}
