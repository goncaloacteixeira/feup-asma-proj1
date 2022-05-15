package utils;

import agents.SubscribableAgent;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public interface ServiceUtils {
    String HUMAN_BROADCAST = "human-broadcast-service";
    String CAR_RIDE = "available-car-ride-service";
    String HUMAN_RESULTS = "human-results-service";

    static DFAgentDescription registerDF(Agent agent) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        try {
            return DFService.register(agent, dfd);
        } catch (FIPAException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers the agent in a DF service.
     *
     * @param agent       The agent to register.
     * @param serviceName The name of the service.
     * @return True if the agent was registered successfully.
     */
    static boolean joinService(SubscribableAgent agent, String serviceName) {
        System.out.println("Registering agent " + agent.getLocalName() + " in service " + serviceName);

        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceName);
        sd.setName(serviceName);

        DFAgentDescription agentDescription = agent.getAgentDescription();
        agentDescription.addServices(sd);

        try {
            agent.setAgentDescription(DFService.modify(agent, agentDescription));
            return true;
        } catch (FIPAException e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean leaveService(SubscribableAgent agent, String serviceName) {
        System.out.printf("Deregistering agent %s from service %s\n", agent.getLocalName(), serviceName);

        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceName);
        sd.setName(serviceName);

        DFAgentDescription agentDescription = agent.getAgentDescription();
        agentDescription.removeServices(sd);

        try {
            agent.setAgentDescription(DFService.modify(agent, agentDescription));
            return true;
        } catch (FIPAException e) {
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

    /**
     * Sends a string message to all agents in the given service.
     *
     * @param agent         the agent that sends the message
     * @param serviceName   the name of the service
     * @param messageString the message to send
     * @param performative  the performative to use
     */
    static void sendStringMessageToService(Agent agent, String serviceName, String messageString, int performative) {
        ACLMessage msg = new ACLMessage(performative);
        Set<DFAgentDescription> agents = ServiceUtils.search(agent, serviceName);
        // add all agents as receivers
        agents.forEach(a -> msg.addReceiver(a.getName()));
        msg.setContent(messageString);
        agent.send(msg);
    }

    /**
     * Sends a message to all agents in the given service.
     *
     * @param agent        the agent that sends the message
     * @param serviceName  the name of the service
     * @param message      the message to send
     * @param performative the performative to use
     * @throws IOException if the message cannot be serialized
     */
    static void sendMessageToService(Agent agent, String serviceName, Serializable message, int performative) throws IOException {
        ACLMessage msg = new ACLMessage(performative);
        Set<DFAgentDescription> agents = ServiceUtils.search(agent, serviceName);
        // add all agents as receivers
        agents.forEach(a -> msg.addReceiver(a.getName()));
        msg.setContentObject(message);
        agent.send(msg);
    }
}
