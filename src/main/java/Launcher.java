import agents.CarAgent;
import agents.HumanAgent;
import agents.HumanPreferences;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

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
            Thread.sleep(1000); // time to initialize

            Launcher.launchCars(container, 2, 4);
            // generateMultipleRandomAgents(container);
            generateTwoAgents(container);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void generateTwoAgents(ContainerController container) throws StaleProxyException {
        HumanPreferences s1 = new HumanPreferences().carShareInitiator().noSubway();
        HumanPreferences s2 = new HumanPreferences().noStreets();
        AgentController ac1 = container.createNewAgent("Human1", HumanAgent.class.getName(), new Object[]{"sem1", "sta5", s1});
        AgentController ac2 = container.createNewAgent("Human2", HumanAgent.class.getName(), new Object[]{"sem1", "sta5", s2});
        ac1.start();
        ac2.start();
    }

    private static void generateMultipleRandomAgents(ContainerController container) throws FileNotFoundException, StaleProxyException {
        List<Point> points = new ArrayList<>(GraphUtils.importGraph("citygraph.dot").vertexSet().stream().toList());

        Random random = new Random();
        List<AgentController> agentControllers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Collections.shuffle(points);
            String p1 = points.get(0).getName();
            String p2 = points.get(1).getName();

            HumanPreferences settings = new HumanPreferences().carShareInitiator(random.nextDouble() > 0.5);
            AgentController ac = container.createNewAgent("Human" + i, HumanAgent.class.getName(), new Object[]{p1, p2, settings});
            agentControllers.add(ac);
        }

        for (AgentController agentController : agentControllers) {
            agentController.start();
        }
    }

    private static void launchCars(ContainerController container, int amount, int carCapacity) throws StaleProxyException {
        List<AgentController> agents = new ArrayList<>();

        Random random = new Random();
        for (int i = 1; i <= amount; i++) {
            AgentController ac = container.createNewAgent("Car" + i, CarAgent.class.getName(), new Object[]{random.nextInt(carCapacity) + 1});
            agents.add(ac);
        }

        for (AgentController agentController : agents) {
            agentController.start();
        }
    }
}
