import agents.BillboardAgent;
import agents.HelloWorldAgent;
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
            AgentController ac = container.createNewAgent("agente bue fixe", "agents.HelloWorldAgent", null);
            AgentController ac2 = container.createNewAgent("Billboard", "agents.BillboardAgent", new Object[]{"HelloWorlders"});

            ac2.start();
            Thread.sleep(1000); // time to initialize
            ac.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
