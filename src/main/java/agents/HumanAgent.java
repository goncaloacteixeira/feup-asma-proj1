package agents;

import behaviours.BroadcastBehaviour;
import behaviours.human.FSMHumanBehaviour;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import utils.ServiceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

public class HumanAgent extends SubscribableAgent {
    private final String broadcastService;
    private String srcPoint;
    private String dstPoint;
    private HumanPreferences settings;
    private EnvironmentPreferences environmentPreferences;

    @Getter
    @Setter
    private DFAgentDescription agentDescription;

    public HumanAgent() {
        this.broadcastService = ServiceUtils.HUMAN_BROADCAST;
    }

    @Override
    protected void setup() {
        Object[] args = this.getArguments();
        this.srcPoint = (String) args[0];                               // Source Point for Travel
        this.dstPoint = (String) args[1];                               // Destiny Point for Travel
        this.settings = (HumanPreferences) args[2];                     // Preferences (weights and initiators)
        this.environmentPreferences = (EnvironmentPreferences) args[3]; // Environment Variables

        // join DF service
        this.agentDescription = ServiceUtils.registerDF(this);

        try {
            // for each agent we need to import a new graph since weights vary from agent to agent
            Graph<Point, DefaultWeightedEdge> graph = GraphUtils.importGraph("citygraph.dot", settings.streetWeight, settings.roadWeight, settings.subwayWeight);
            Graph<Point, DefaultWeightedEdge> original = GraphUtils.importGraph("citygraph.dot", settings.streetWeight, settings.roadWeight, settings.subwayWeight);

            // add Finite State Machine Behaviour
            addBehaviour(new FSMHumanBehaviour(this, graph, original, srcPoint, dstPoint, settings));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void informMovement(String message) {
        addBehaviour(new BroadcastBehaviour(this, ACLMessage.INFORM, message, this.broadcastService));
    }

    public void informResults(Serializable content) {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                Set<DFAgentDescription> agents = ServiceUtils.search(this.myAgent, ServiceUtils.HUMAN_RESULTS);

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                for (DFAgentDescription agent : agents) {
                    msg.addReceiver(agent.getName());
                }

                try {
                    msg.setContentObject(content);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }

                this.myAgent.send(msg);
            }
        });
    }

    public EnvironmentPreferences getEnvironmentPreferences() {
        return environmentPreferences;
    }

    @Override
    protected void takeDown() {
        System.out.printf("%s: Went from %s to %s%n", getLocalName(), srcPoint, dstPoint);
    }
}
