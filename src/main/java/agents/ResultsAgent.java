package agents;

import com.opencsv.CSVWriter;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import messages.results.CarService;
import messages.results.PathEnd;
import messages.results.PathStart;
import messages.results.ShareRide;
import utils.ServiceUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class AgentResults {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String path;

    @Getter
    @Setter
    private double originalCost;

    @Getter
    @Setter
    private double finalCost;

    @Getter
    private final List<SharedSegment> sharedSegments = new ArrayList<>();

    @Getter
    private final List<CarServiceFare> carServiceFares = new ArrayList<>();

    public AgentResults(String name, String path, double originalCost) {
        this.name = name;
        this.path = path;
        this.originalCost = originalCost;
    }

    @Override
    public String toString() {
        return "AgentResults{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", originalCost=" + originalCost +
                ", finalCost=" + finalCost +
                ", sharedSegments=" + sharedSegments +
                ", carServiceFares=" + carServiceFares +
                '}';
    }

    public String[] valuesToWrite() {
        return new String[]{name, path, String.valueOf(originalCost), String.valueOf(finalCost), sharedSegments.toString(), carServiceFares.toString()};
    }
}

record SharedSegment(String path, boolean initiator) implements Serializable {
    @Override
    public String toString() {
        return String.format("Path: %s, Initiator: %s", path, initiator);
    }
}

record CarServiceFare(String path, double fare, double expectedCost) implements Serializable {
    @Override
    public String toString() {
        return String.format("Path: %s, Fare: %.02f, Expected: %.02f", path, fare, expectedCost);
    }
}

class ListenResultsBehaviour extends CyclicBehaviour {
    private final ConcurrentHashMap<String, AgentResults> map;
    private final File file;

    public ListenResultsBehaviour(ConcurrentHashMap<String, AgentResults> map, String path) {
        this.map = map;
        this.file = new File(path);
    }

    @Override
    public void onStart() {
        ServiceUtils.joinService((ResultsAgent) myAgent, ServiceUtils.HUMAN_RESULTS);
    }

    @SneakyThrows
    @Override
    public void action() {
        ACLMessage message = myAgent.receive();
        if (message != null) {
            if (message.getPerformative() == ACLMessage.INFORM) {
                Serializable obj = message.getContentObject();
                if (obj instanceof PathStart aux) {
                    map.put(aux.name(), new AgentResults(aux.getName(), aux.path(), aux.cost()));
                }
                if (obj instanceof ShareRide aux) {
                    map.get(aux.name()).getSharedSegments().add(new SharedSegment(aux.path(), aux.initiator()));
                }
                if (obj instanceof CarService aux) {
                    map.get(aux.name()).getCarServiceFares().add(new CarServiceFare(aux.path(), aux.fare(), aux.expectedCost()));
                }
                if (obj instanceof PathEnd aux) {
                    map.get(aux.name()).setFinalCost(aux.finalCost());

                    FileWriter outputfile = new FileWriter(file);

                    CSVWriter writer = new CSVWriter(outputfile);

                    String[] header = {"name", "path", "original_cost", "final_cost", "shared_segments", "car_service_fares"};
                    writer.writeNext(header);

                    for (Map.Entry<String, AgentResults> entry : this.map.entrySet()) {
                        writer.writeNext(entry.getValue().valuesToWrite());
                    }

                    writer.close();
                }
            }
        }
    }
}

public class ResultsAgent extends SubscribableAgent {
    @Getter
    @Setter
    private DFAgentDescription agentDescription;

    private final ConcurrentHashMap<String, AgentResults> resultsMap = new ConcurrentHashMap<>();

    @Override
    public void setup() {
        Object[] args = this.getArguments();
        String path = (String) args[0];

        // register the DF
        this.agentDescription = ServiceUtils.registerDF(this);

        addBehaviour(new ListenResultsBehaviour(resultsMap, path));
    }
}
