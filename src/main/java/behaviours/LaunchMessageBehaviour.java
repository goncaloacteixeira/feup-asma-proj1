package behaviours;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

/**
 * Makes it so that a message is displayed when the agent is launched.
 */
public class LaunchMessageBehaviour extends Behaviour {

    private final String message;

    private boolean done;

    public LaunchMessageBehaviour(Agent agent, String message) {
        super(agent);

        this.message = message;

        this.done = false;
    }

    @Override
    public void action() {
        System.out.printf("%s: %s%n", this.myAgent.getLocalName(), message);
        done = true;
    }

    @Override
    public boolean done() {
        return done;
    }
}
