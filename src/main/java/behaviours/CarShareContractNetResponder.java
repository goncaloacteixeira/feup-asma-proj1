package behaviours;

import graph.RoadPathPoints;
import graph.vertex.Point;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetResponder;
import jade.proto.SSContractNetResponder;
import jade.proto.SSIteratedContractNetResponder;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CarShareContractNetResponder extends SSContractNetResponder {
    private final Graph<Point, DefaultWeightedEdge> graph;
    private Pair<String, Boolean> done;
    private GraphPath<Point, DefaultWeightedEdge> roadPath;

    public CarShareContractNetResponder(Agent a, ACLMessage cfp, Pair<String, Boolean> done, GraphPath<Point, DefaultWeightedEdge> roadPath, Graph<Point, DefaultWeightedEdge> graph) {
        super(a, cfp);
        this.done = done;
        this.roadPath = roadPath;
        this.graph = graph;
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
        if (!roadPath.getStartVertex().getName().equals(pathPoints.srcPoint) || !roadPath.getEndVertex().getName().equals(pathPoints.dstPoint)) {
            ACLMessage refusal = cfp.createReply();
            refusal.setPerformative(ACLMessage.REFUSE);
            return refusal;
        }

        double proposal = new Random().nextGaussian(0.4, 0.15);
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

        Double[] contribs = new Double[roadPath.getEdgeList().size()];
        for (int i = 0; i < roadPath.getEdgeList().size(); i++) {
            DefaultWeightedEdge e = roadPath.getEdgeList().get(i);
            Double weight = graph.getEdgeWeight(e);
            graph.setEdgeWeight(e,  weight * contrib);
            contribs[i] = weight * contrib;
        }

        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        try {
            inform.setContentObject(contribs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
