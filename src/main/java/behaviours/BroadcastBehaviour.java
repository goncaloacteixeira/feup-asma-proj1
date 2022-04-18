package behaviours;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import utils.ServiceUtils;

import java.util.Set;

public class BroadcastBehaviour extends Behaviour {

    private final int performative;

    private final String content;

    private final String serviceName;

    private boolean done;

    public BroadcastBehaviour(Agent a, int performative, String content, String serviceName) {
        super(a);

        this.done = false;

        this.performative = performative;
        this.content = content;
        this.serviceName = serviceName;
    }

    @Override
    public void action() {
        // gets all agents
        Set<DFAgentDescription> agents = ServiceUtils.search(this.myAgent, this.serviceName);

        ACLMessage msg = new ACLMessage(this.performative);
        for (DFAgentDescription agent : agents) {
            msg.addReceiver(agent.getName());
        }

        msg.setContent(this.content);

        this.myAgent.send(msg);
        this.done = true;
    }

    @Override
    public boolean done() {
        return this.done;
    }
}
