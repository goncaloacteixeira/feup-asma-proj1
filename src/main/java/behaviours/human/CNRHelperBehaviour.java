package behaviours.human;

import agents.HumanAgent;
import graph.GraphUtils;
import graph.exceptions.NoRoadsException;
import graph.vertex.Point;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import lombok.Setter;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;


class CNRHelperBehaviour extends Behaviour {
    private final FSMHumanBehaviour fsmHumanBehaviour;
    private final MessageTemplate messageTemplate;
    private final Pair<String, Boolean> done = Pair.of("done", false);
    private boolean busy = false;
    private int attempts = 0;
    /**
     * If a share was agreed
     */
    @Setter
    private boolean agreed = false;

    /**
     * Behaviour to create a new ContractNet Responder behaviour
     *
     * @param fsmHumanBehaviour parent behaviour
     */
    public CNRHelperBehaviour(FSMHumanBehaviour fsmHumanBehaviour) {
        super(fsmHumanBehaviour.getAgent());
        this.fsmHumanBehaviour = fsmHumanBehaviour;
        this.messageTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));
    }

    @Override
    public void onStart() {
        try {
            Point p1 = fsmHumanBehaviour.path.getVertexList().get(fsmHumanBehaviour.currentLocationIndex);
            Point p2 = GraphUtils.roadStop(fsmHumanBehaviour.graph, fsmHumanBehaviour.path, fsmHumanBehaviour.currentLocationIndex);
            // Humans either init car share or respond to car sharing when they start a new road travel
            ServiceUtils.joinService((HumanAgent) this.myAgent, ServiceUtils.buildShareName(p1.getName(), p2.getName()));

            System.out.printf("%s: Requesting Car Share from %s to %s\n", myAgent.getLocalName(), p1, p2);
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
        var result =  this.agreed ? FSMHumanBehaviour.EVENT_FOUND_SHARE : this.nothingFound();
        this.reset();
        return result;
    }

    @Override
    public void reset() {
        this.busy = false;
        this.done.setSecond(false);
        this.attempts = 0;
        this.agreed = false;
        super.reset();
    }

    private int nothingFound() {
        this.fsmHumanBehaviour.setCurrentCarService(ServiceUtils.buildRideName(myAgent.getLocalName()));
        ServiceUtils.joinService((HumanAgent) this.myAgent, ServiceUtils.buildRideName(this.myAgent.getLocalName()));

        return super.onEnd();
    }

    /**
     * Busy waiting until a Call For Proposal is received or it timeouts. After receiving a CFP it
     * starts a new Contract Net Responder behaviour
     */
    @Override
    public void action() {
        if (!busy) {
            block(1000);
            ACLMessage cfp = myAgent.receive(messageTemplate);
            if (cfp != null) {
                try {
                    Point p1 = fsmHumanBehaviour.path.getVertexList().get(fsmHumanBehaviour.currentLocationIndex);
                    Point p2 = GraphUtils.roadStop(fsmHumanBehaviour.graph, fsmHumanBehaviour.path, fsmHumanBehaviour.currentLocationIndex);

                    GraphPath<Point, DefaultWeightedEdge> roadPath = GraphUtils.getPathFromAtoB(fsmHumanBehaviour.graph, p1.getName(), p2.getName());

                    Behaviour behaviour = new CarShareContractNetResponder(this.fsmHumanBehaviour, this, cfp, done, roadPath, fsmHumanBehaviour.graph);
                    busy = true;
                    myAgent.addBehaviour(behaviour);
                } catch (NoRoadsException e) {
                    done.setSecond(Boolean.TRUE);
                }
            } else {
                attempts++;
                if (attempts >= 10) {
                    System.out.printf("%s: Max Attempts Reached!\n", myAgent.getLocalName());
                    done.setSecond(Boolean.TRUE);
                }
            }
        }
    }
}
