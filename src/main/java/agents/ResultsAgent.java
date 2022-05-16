package agents;

import com.opencsv.CSVWriter;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import messages.results.PathEnd;
import utils.ServiceUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ListenResultsBehaviour extends CyclicBehaviour {
    private final ConcurrentHashMap<String, HumanResults> map;
    private final File file;

    public ListenResultsBehaviour(ConcurrentHashMap<String, HumanResults> map, String path) throws IOException {
        this.map = map;
        this.file = new File(path);
    }

    @Override
    public void onStart() {
        ServiceUtils.joinService((ResultsAgent) myAgent, ServiceUtils.HUMAN_RESULTS);
        FileWriter outputfile = null;
        try {
            outputfile = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var writer = new CSVWriter(outputfile);

        String[] header = {"name", "path", "initiator", "original_cost", "final_cost", "shared_segments", "car_service_fares", "num_shared_segments", "num_car_service_fares"};
        writer.writeNext(header);
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    @Override
    public void action() {
        ACLMessage message = myAgent.receive();
        FileWriter outputfile = new FileWriter(file, true);
        var writer = new CSVWriter(outputfile);
        if (message != null) {
            if (message.getPerformative() == ACLMessage.INFORM) {
                HumanResults obj = (HumanResults) message.getContentObject();
                writer.writeNext(obj.valuesToWrite());
            }
        }
        writer.close();
    }
}

public class ResultsAgent extends SubscribableAgent {
    @Getter
    @Setter
    private DFAgentDescription agentDescription;

    private final ConcurrentHashMap<String, HumanResults> resultsMap = new ConcurrentHashMap<>();

    @Override
    public void setup() {
        Object[] args = this.getArguments();
        String path = (String) args[0];

        // register the DF
        this.agentDescription = ServiceUtils.registerDF(this);

        try {
            addBehaviour(new ListenResultsBehaviour(resultsMap, path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
