package utils;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public interface ServiceUtils {

    String HUMAN_BROADCAST = "human-broadcast-service";

    String CAR = "car-service";

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

    static boolean deregister(Agent agent) {
        try {
            DFService.deregister(agent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static DFAgentDescription[] search(Agent agent, String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceName);
        dfd.addServices(sd);

        try {
            return DFService.search(agent, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DFAgentDescription[0];
    }
}
