package behaviours.human;

import graph.vertex.Point;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Enumeration;
import java.util.Vector;

public class CarShareContractNetInitiator extends ContractNetInitiator {
    private final Pair<String, Boolean> done;
    private final Graph<Point, DefaultWeightedEdge> graph;
    private int nResponders;
    private final GraphPath<Point, DefaultWeightedEdge> roadPath;

    public CarShareContractNetInitiator(Agent a, ACLMessage cfp, int nResponders, Pair<String, Boolean> done, GraphPath<Point, DefaultWeightedEdge> roadPath, Graph<Point, DefaultWeightedEdge> graph) {
        super(a, cfp);
        this.nResponders = nResponders;
        this.done = done;
        this.roadPath = roadPath;
        this.graph = graph;
    }

    protected void handlePropose(ACLMessage propose, Vector v) {
        System.out.printf("%s: %s proposed %s\n", myAgent.getLocalName(), propose.getSender().getLocalName(), propose.getContent());
    }

    protected void handleRefuse(ACLMessage refuse) {
        System.out.printf("%s: %s refused\n", myAgent.getLocalName(), refuse.getSender().getLocalName());
    }

    protected void handleFailure(ACLMessage failure) {
        if (failure.getSender().equals(myAgent.getAMS())) {
            // FAILURE notification from the JADE runtime: the receiver
            // does not exist
            System.out.println("Responder does not exist");
        } else {
            System.out.println("Agent " + failure.getSender().getName() + " failed");
        }
        // Immediate failure --> we will not receive a response from this agent
        nResponders--;
    }

    protected void handleAllResponses(Vector responses, Vector acceptances) {
        if (responses.size() < nResponders) {
            // Some responder didn't reply within the specified timeout
            System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
        }

        /*
         * Evaluate Proposal
         * - threshold: max acceptable contributions from proposers
         * - current: current contributions from proposers
         *
         * While the current does not reach threshold, we accept proposals.
         * TODO - account for car capacity, add a new field to this class after uber find behaviour @marcio
         */
        double current = 0.0;
        double threshold = 0.9;
        Enumeration e = responses.elements();
        while (e.hasMoreElements()) {
            ACLMessage msg = (ACLMessage) e.nextElement();
            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                ACLMessage reply = msg.createReply();
                double proposal = Double.parseDouble(msg.getContent());
                if (current + proposal < threshold) {
                    current += proposal;
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                } else {
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }
                acceptances.addElement(reply);
            }
        }
    }

    protected void handleInform(ACLMessage inform) {
        System.out.printf("%s: %s completed ContractNet\n", myAgent.getLocalName(), inform.getSender().getLocalName());
        try {
            /*
             * Decrement weight based on contributions. The INFORM message has a double[] containing the
             * contributions from the proposer
             */
            Double[] contributions = (Double[]) inform.getContentObject();
            for (int i = 0; i < roadPath.getEdgeList().size(); i++) {
                DefaultWeightedEdge e = roadPath.getEdgeList().get(i);
                double weight = graph.getEdgeWeight(e);
                graph.setEdgeWeight(e, weight - contributions[i]);
            }
        } catch (UnreadableException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onEnd() {
        reset();
        this.done.setSecond(Boolean.TRUE);
        return 0;
    }
}
