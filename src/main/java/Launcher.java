import agents.*;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.Boot;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.jgrapht.Graph;

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
            // AgentController ac = container.createNewAgent("agente bue fixe", "agents.HelloWorldAgent", null);
            AgentController billboardController = container.createNewAgent("Billboard", "agents.BillboardAgent", new Object[]{"human-broadcast-service"});
            billboardController.start();
            Thread.sleep(1000); // time to initialize

            // generateMultipleRandomAgents(container);
            generateTwoAgents(container);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void generateTwoAgents(ContainerController container) throws StaleProxyException {
        AgentController ac1 = container.createNewAgent("Human1", HumanAgent.class.getName(), new Object[]{"sem1", "sem6", true});
        AgentController ac2 = container.createNewAgent("Human2", HumanAgent.class.getName(), new Object[]{"sem1", "sem6", false});
        ac1.start();
        ac2.start();
    }

    private static void generateMultipleRandomAgents(ContainerController container) throws FileNotFoundException, StaleProxyException {
        List<Point> points = new ArrayList<>(GraphUtils.importGraph("citygraph.dot").vertexSet().stream().toList());

        Random random = new Random();
        List<AgentController> agentControllers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
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
}
