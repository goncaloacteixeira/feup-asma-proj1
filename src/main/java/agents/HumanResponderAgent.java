package agents;

import behaviours.BroadcastBehaviour;
import behaviours.CarShareContractNetResponder;
import graph.CityGraph;
import graph.vertex.Point;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

public class HumanResponderAgent extends Agent {
    private final String broadcastService;
    private String srcPoint;
    private String dstPoint;

    public HumanResponderAgent() {
        this.broadcastService = "human-broadcast-service";
    }

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.srcPoint = (String) args[0];
        this.dstPoint = (String) args[1];

        String broadcastMessage = String.format("I will go from %s to %s", srcPoint, dstPoint);
        Graph<Point, DefaultWeightedEdge> graph = CityGraph.importGraph("citygraph.dot");
        ServiceUtils.register(this, "human-responders");

        addBehaviour(new BroadcastBehaviour(this, ACLMessage.INFORM, broadcastMessage, this.broadcastService));

        // Respond to ContractNet
        System.out.println("Agent " + getLocalName() + " waiting for CFP...");
        MessageTemplate template = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                MessageTemplate.MatchPerformative(ACLMessage.CFP));

        addBehaviour(new CarShareContractNetResponder(this, template));

    }

    @Override
    protected void takeDown() {
        System.out.printf("%s: Went from %s to %s%n", getLocalName(), srcPoint, dstPoint);
    }

}
