package agents;

import behaviours.BroadcastBehaviour;
import behaviours.CarShareContractNetInitiator;
import graph.CityGraph;
import graph.vertex.Point;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.util.Arrays;
import java.util.Date;

public class HumanInitiatorAgent extends Agent {
    private final String broadcastService;
    private String srcPoint;
    private String dstPoint;

    public HumanInitiatorAgent() {
        this.broadcastService = "human-broadcast-service";
    }

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.srcPoint = (String) args[0];
        this.dstPoint = (String) args[1];

        String broadcastMessage = String.format("I will go from %s to %s", srcPoint, dstPoint);
        Graph<Point, DefaultWeightedEdge> graph = CityGraph.importGraph("citygraph.dot");
        ServiceUtils.register(this, "human-initiators");

        addBehaviour(new BroadcastBehaviour(this, ACLMessage.INFORM, broadcastMessage, this.broadcastService));

        // Initiate ContractNet
        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
        // Deadline is 10s after message is sent
        msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
        msg.setContent("dummy-action");

        // procurar malta que está a procura de carro
        DFAgentDescription[] agents = ServiceUtils.search(this, "human-responders");

        Arrays.stream(agents)
                .forEach(agent -> msg.addReceiver(agent.getName()));

        addBehaviour(new CarShareContractNetInitiator(this, msg, agents.length));
    }

    @Override
    protected void takeDown() {
        System.out.printf("%s: Went from %s to %s%n", getLocalName(), srcPoint, dstPoint);
    }
}
