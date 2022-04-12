package behaviours;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import utils.ServiceUtils;

/**
 * Registers in a service so that it can display all messages that were broadcast there.
 */
public class DisplayMessagesBehaviour extends Behaviour {

    private final String service;

    private boolean done;

    public DisplayMessagesBehaviour(Agent agent, String service) {
        super(agent);

        this.done = false;

        this.service = service;

        boolean registered = ServiceUtils.register(this.myAgent, this.service);
        if (!registered) {
            System.out.println("Could not register in service " + this.service);
            this.done = true;
        }
    }

    @Override
    public void action() {
        // listens for messages
        ACLMessage msg = this.myAgent.receive();

        if (msg != null) {
            System.out.printf("%s: %s: %s%n", this.service, msg.getSender().getLocalName(), msg.getContent());
        } else {
            this.block();
        }
    }

    @Override
    public boolean done() {
        return this.done;
    }

    public void stop() {
        this.done = true;
        boolean unregistered = ServiceUtils.deregister(this.myAgent);
        if (!unregistered) {
            System.out.println("Could not unregister from service " + this.service);
        }
    }
}
