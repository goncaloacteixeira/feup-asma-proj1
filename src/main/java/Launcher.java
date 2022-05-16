import agents.*;
import graph.GraphUtils;
import graph.vertex.Point;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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

            Launcher.launchCars(container, 20, 4);
            generateMultipleRandomAgents(container, 50);
            //generateTwoAgents(container);

            /*AgentController results = container.createNewAgent(
                    "Results",
                    ResultsAgent.class.getName(),
                    new Object[]{String.format("./results/results-%s.csv", new Date().getTime())});
            results.start();*/
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void generateTwoAgents(ContainerController container) throws StaleProxyException {
        HumanPreferences s1 = new HumanPreferences().carShareInitiator().noSubway();
        HumanPreferences s2 = new HumanPreferences().noStreets();

        EnvironmentPreferences ep = new EnvironmentPreferences(3.5);

        AgentController ac1 = container.createNewAgent("Human1", HumanAgent.class.getName(), new Object[]{"sem1", "sta5", s1, ep});
        AgentController ac2 = container.createNewAgent("Human2", HumanAgent.class.getName(), new Object[]{"sem1", "sta5", s2, ep});
        ac1.start();
        ac2.start();
    }

    private static void generateMultipleRandomAgents(ContainerController container, int numberAgents) throws FileNotFoundException, StaleProxyException {
        List<Point> points = new ArrayList<>(GraphUtils.importGraph("citygraph.dot").vertexSet().stream().toList());

        Random random = new Random();
        List<AgentController> agentControllers = new ArrayList<>();

        EnvironmentPreferences ep = new EnvironmentPreferences(3.5);

        for (int i = 1; i <= numberAgents; i++) {
            Collections.shuffle(points);
            String p1 = points.get(0).getName();
            String p2 = points.get(1).getName();

           /* Graph<Point, DefaultWeightedEdge> graph = GraphUtils.importGraph("citygraph.dot");
            var path = GraphUtils.getPathFromAtoB(graph, p1, p2);
            if (path.getVertexList().size() <= 5) {
                i--;
                continue;
            }*/

            if (random.nextDouble() > 0.8) {
                int waiters = ThreadLocalRandom.current().nextInt(1, 5 + 1);;
                for (int j = 1; j <= waiters; j++) {
                    HumanPreferences settings = new HumanPreferences().carShareInitiator(false);
                    AgentController ac = container.createNewAgent(String.format("Human-%d-Waiter-%d", i, j), HumanAgent.class.getName(), new Object[]{p1, p2, settings, ep});
                    agentControllers.add(ac);
                }
            }

            HumanPreferences settings = new HumanPreferences().carShareInitiator(true);
            AgentController ac = container.createNewAgent("Human" + i, HumanAgent.class.getName(), new Object[]{p1, p2, settings, ep});
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
