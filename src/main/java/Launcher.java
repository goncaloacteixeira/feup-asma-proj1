import agents.*;
import jade.Boot;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Launcher {
    public static void main(String[] args) {
        Profile p = new ProfileImpl();

        // if GUI is in the args
        if (args.length > 0 && args[0].equals("-gui")) {
            p.setParameter(Profile.GUI, "true");
        }

        Runtime rt = Runtime.instance();

        rt.setCloseVM(true);
        ContainerController container = rt.createMainContainer(p);

        Launcher.launchAgents(container);
    }

    private static void launchAgents(ContainerController container) {
        try {
            // AgentController ac = container.createNewAgent("agente bue fixe", "agents.HelloWorldAgent", null);
            AgentController billboardController = container.createNewAgent("Billboard", "agents.BillboardAgent", new Object[]{"human-broadcast-service"});
            billboardController.start();
            Thread.sleep(1000); // time to initialize

            // AgentController humanC1 = container.createNewAgent("Human1", HumanAgent.class.getName(), new Object[]{"sem1", "sta3"});
            // AgentController humanC2 = container.createNewAgent("Human2", HumanAgent.class.getName(), new Object[]{"sem2", "sta4"});

            AgentController h1 = container.createNewAgent(
                    "Human1",
                    HumanInitiatorAgent.class.getName(),
                    new Object[]{"sem1", "sta3"}
            );

            for (int i = 2; i < 4; i++) {
                container.createNewAgent(
                        "Human" + i,
                        HumanResponderAgent.class.getName(),
                        new Object[]{"sem1", "sta3"}
                ).start();
            }

            h1.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
