import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class HelloWorldAgent extends Agent {
    public void setup() {
        addBehaviour(new HelloWorldBehaviour());
        System.out.println(getLocalName() + ": Setup Completed");
    }

    public void takeDown() {
        System.out.println(getLocalName() + ": Said hello world. Leaving now.");
    }

    static class HelloWorldBehaviour extends Behaviour {
        private boolean done = false;

        public void action() {
            System.out.println("Hello World");
            done = true;
        }

        public boolean done() {
            return done;
        }
    }
}
