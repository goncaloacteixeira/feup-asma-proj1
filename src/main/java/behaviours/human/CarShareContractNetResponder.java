package behaviours.human;

import agents.HumanAgent;
import graph.RoadPathPoints;
import graph.vertex.Point;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.SSContractNetResponder;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.io.IOException;
import java.util.Random;

import static org.apache.commons.math3.util.Precision.round;

public class CarShareContractNetResponder extends SSContractNetResponder {
    private final Graph<Point, DefaultWeightedEdge> graph;
    private final Pair<String, Boolean> done;
    private final GraphPath<Point, DefaultWeightedEdge> roadPath;
    private final FSMHumanBehaviour fsmHumanBehaviour;
    private final CNRHelperBehaviour cnrHelperBehaviour;

    public CarShareContractNetResponder(FSMHumanBehaviour fsmHumanBehaviour, CNRHelperBehaviour cnrHelperBehaviour, ACLMessage cfp, Pair<String, Boolean> done, GraphPath<Point, DefaultWeightedEdge> roadPath, Graph<Point, DefaultWeightedEdge> graph) {
        super(fsmHumanBehaviour.getAgent(), cfp);
        this.done = done;
        this.roadPath = roadPath;
        this.graph = graph;

        this.fsmHumanBehaviour = fsmHumanBehaviour;
        this.cnrHelperBehaviour = cnrHelperBehaviour;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
        RoadPathPoints pathPoints;
        try {
            pathPoints = (RoadPathPoints) cfp.getContentObject();
            System.out.printf("%s: CFP(%s): %s\n", myAgent.getLocalName(), cfp.getSender().getLocalName(), pathPoints);
        } catch (UnreadableException e) {
            throw new NotUnderstoodException("wrong-class");
        }

        // Only accept equal segment paths
        if (!roadPath.getStartVertex().getName().equals(pathPoints.srcPoint()) || !roadPath.getEndVertex().getName().equals(pathPoints.dstPoint())) {
            ACLMessage refusal = cfp.createReply();
            refusal.setPerformative(ACLMessage.REFUSE);
            return refusal;
        }

        double proposal = new Random().nextGaussian(0.4, 0.1);
        proposal = round(proposal, 1);

        System.out.printf("%s: Proposing: %.02f\n", myAgent.getLocalName(), proposal);

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);
        propose.setContent(String.valueOf(proposal));
        return propose;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
        System.out.printf("%s: Proposal Accepted\n", myAgent.getLocalName());
        Double contrib = Double.valueOf(propose.getContent());

        /*
         * Contributions are calculated based on contrib value (0.5, 0.3, ...), then a contribution value is
         * calculated based on the original edge weight, for the whole road path
         */
        Double[] contributions = new Double[roadPath.getEdgeList().size()];
        for (int i = 0; i < roadPath.getEdgeList().size(); i++) {
            DefaultWeightedEdge e = roadPath.getEdgeList().get(i);
            Double weight = graph.getEdgeWeight(e);
            graph.setEdgeWeight(e,  weight * contrib);
            contributions[i] = weight * contrib;
        }

        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        try {
            inform.setContentObject(contributions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // join the other agent ride service so that the car can communicate with both
        this.fsmHumanBehaviour.setCurrentCarService(ServiceUtils.buildRideName(accept.getSender().getLocalName()));
        ServiceUtils.joinService((HumanAgent) this.myAgent, this.fsmHumanBehaviour.getCurrentCarService());

        this.cnrHelperBehaviour.setAgreed(true);

        return inform;
    }

    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.printf("%s: Proposal Rejected\n", myAgent.getLocalName());
    }

    public int onEnd() {
        this.done.setSecond(Boolean.TRUE);
        reset();
        return super.onEnd();
    }
}
