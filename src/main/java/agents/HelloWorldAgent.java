package agents;

import behaviours.BroadcastBehaviour;
import behaviours.LaunchMessageBehaviour;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class HelloWorldAgent extends Agent {

    private final String broadcastService;

    public HelloWorldAgent() {
        this.broadcastService = "HelloWorlders";
    }

    @Override
    protected void setup() {
        addBehaviour(new LaunchMessageBehaviour(this, "Hello World!"));
        addBehaviour(new BroadcastBehaviour(this, ACLMessage.INFORM, "Hello World!", this.broadcastService));
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + ": Said hello world. Leaving now.");
    }
}
