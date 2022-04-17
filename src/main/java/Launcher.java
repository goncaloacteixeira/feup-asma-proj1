import agents.BillboardAgent;
import agents.CarAgent;
import agents.HumanAgent;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import utils.ServiceUtils;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
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
            AgentController billboardController = container.createNewAgent("Billboard", BillboardAgent.class.getName(), new Object[]{ServiceUtils.HUMAN_BROADCAST});
            billboardController.start();
            Thread.sleep(1000); // time to initialize

            Launcher.launchHumans(container, 5);
            Launcher.launchCars(container, 2, 4);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void launchHumans(ContainerController container, int amount) throws StaleProxyException, FileNotFoundException {
        List<Point> points = new java.util.ArrayList<>(GraphUtils.importDefaultGraph().vertexSet().stream().toList());
        List<AgentController> agentControllers = new ArrayList<>();

        Random random = new Random();
        for (int i = 1; i <= amount; i++) {
            Collections.shuffle(points);
            byte[] p1 = points.get(0).getName().getBytes(StandardCharsets.UTF_8);
            byte[] p2 = points.get(1).getName().getBytes(StandardCharsets.UTF_8);

            boolean initiator = random.nextDouble() > 0.5;
            AgentController ac = container.createNewAgent("Human" + i, HumanAgent.class.getName(), new Object[]{new String(p1, StandardCharsets.UTF_8), new String(p2, StandardCharsets.UTF_8), initiator});
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
