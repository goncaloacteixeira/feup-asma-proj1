package agents;

import behaviours.BroadcastBehaviour;
import behaviours.human.FSMHumanBehaviour;
import com.opencsv.CSVWriter;
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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Set;



public class HumanAgent extends SubscribableAgent {
    private final String broadcastService;
    private String srcPoint;
    private String dstPoint;
    @Getter
    private HumanPreferences settings;
    private EnvironmentPreferences environmentPreferences;
    @Getter
    @Setter
    private HumanResults results;

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

    public void informResults() {
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                FileWriter outputfile = null;
                try {
                    outputfile = new FileWriter(String.format("./results/%s-results-%s.csv", myAgent.getLocalName(), new Date().getTime()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                var writer = new CSVWriter(outputfile);

                String[] header = {"name", "path", "initiator", "original_cost", "final_cost", "shared_segments", "car_service_fares", "num_shared_segments", "num_car_service_fares"};
                writer.writeNext(header);

                writer.writeNext(results.valuesToWrite());

                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                /*Set<DFAgentDescription> agents = ServiceUtils.search(this.myAgent, ServiceUtils.HUMAN_RESULTS);

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                for (DFAgentDescription agent : agents) {
                    msg.addReceiver(agent.getName());
                }

                try {
                    msg.setContentObject(results);
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }

                this.myAgent.send(msg);*/
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
