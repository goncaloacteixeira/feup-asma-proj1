package utils;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.HashSet;
import java.util.Set;

public interface ServiceUtils {

    String HUMAN_BROADCAST = "human-broadcast-service";

    String CAR_RIDE = "available-car-ride-service";

    /**
     * Registers the agent in a DF service.
     *
     * @param agent       The agent to register.
     * @param serviceName The name of the service.
     * @return True if the agent was registered successfully.
     */
    static boolean register(Agent agent, String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceName);
        sd.setName(agent.getLocalName() + "-" + serviceName);
        dfd.addServices(sd);

        try {
            DFService.register(agent, dfd);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean deregister(Agent agent, String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceName);
        sd.setName(agent.getLocalName() + "-" + serviceName);
        dfd.addServices(sd);

        try {
            DFService.deregister(agent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static Set<DFAgentDescription> search(Agent agent, String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceName);
        dfd.addServices(sd);

        try {
            DFAgentDescription[] search = DFService.search(agent, dfd);
            return new HashSet<>(java.util.Arrays.asList(search));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HashSet<>(0);
    }

    static String buildRideName(String agentName) {
        return agentName + "-" + "ride";
    }
}
