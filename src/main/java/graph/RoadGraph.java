package graph;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.GraphIterables;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultGraphIterables;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.dot.DOTImporter;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class RoadGraph {
    private RoadGraph() {
    }

    public static Graph<Point, DefaultWeightedEdge> buildGridGraph(int size) {
        Graph<Point, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(RoadEdge.class);
        List<Semaphore> v = new ArrayList<>();

        for (int i = 0; i < size * size; i++) {
            graph.addVertex(new Semaphore(String.format("sem%d", i + 1)));
            v.add(new Semaphore(String.format("sem%d", i + 1)));
        }

        for (int i = 0; i < size * size; i += size) {
            for (int j = 0; j < size - 1; j++) {
                DefaultWeightedEdge e = graph.addEdge(v.get(i + j), v.get(i + j + 1));
                graph.setEdgeWeight(e, 1);
            }
        }

        for (int i = 0; i < size * size - size; i += size) {
            for (int j = 0; j < size; j++) {
                DefaultWeightedEdge e = graph.addEdge(v.get(i + j), v.get(i + j + size));
                graph.setEdgeWeight(e, 1);
            }
        }

        // road edges weight, more weight on denser zones
        GraphIterables<Point, DefaultWeightedEdge> graphIterables = new DefaultGraphIterables<>(graph);
        for (DefaultWeightedEdge edge : graphIterables.edges()) {
            int degree1 = graph.inDegreeOf(graph.getEdgeSource(edge));
            int degree2 = graph.inDegreeOf(graph.getEdgeTarget(edge));

            graph.setEdgeWeight(edge, (degree1 + degree2) / 2.0);
        }

        return graph;
    }

    public static Graph<Point, DefaultWeightedEdge> buildRandomGraph(int size) {
        Graph<Point, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(RoadEdge.class);
        List<Semaphore> v = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            graph.addVertex(new Semaphore(String.format("sem%d", i + 1)));
            v.add(new Semaphore(String.format("sem%d", i + 1)));
        }

        ConnectivityInspector<Point, DefaultWeightedEdge> inspector = new ConnectivityInspector<>(graph);
        while (!inspector.isConnected()) {
            Collections.shuffle(v);
            List<Semaphore> rnd = v.subList(0, 2);
            graph.addEdge(rnd.get(0), rnd.get(1));
            inspector = new ConnectivityInspector<>(graph);
        }

        // road edges weight, more weight on denser zones
        GraphIterables<Point, DefaultWeightedEdge> graphIterables = new DefaultGraphIterables<>(graph);
        for (DefaultWeightedEdge edge : graphIterables.edges()) {
            int degree1 = graph.inDegreeOf(graph.getEdgeSource(edge));
            int degree2 = graph.inDegreeOf(graph.getEdgeTarget(edge));

            graph.setEdgeWeight(edge, (degree1 + degree2) / 2.0);
        }

        return graph;
    }

    public static GraphPath<Point, DefaultWeightedEdge> getPathFromAtoB(Graph<Point, DefaultWeightedEdge> graph, String a, String b) {
        DijkstraShortestPath<Point, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        ShortestPathAlgorithm.SingleSourcePaths<Point, DefaultWeightedEdge> iPaths = dijkstraAlg.getPaths(Point.instance(a));

        return iPaths.getPath(Point.instance(b));
    }

    public static Graph<Point, DefaultWeightedEdge> getFromDOTFile(String filename) {
        DOTImporter<Point, DefaultWeightedEdge> importer = new DOTImporter<>();
        Graph<Point, DefaultWeightedEdge> g = new SimpleWeightedGraph<>(RoadEdge.class);
        importer.setVertexFactory(Semaphore::new);
        importer.setEdgeWithAttributesFactory((map) -> {
            DefaultWeightedEdge edge = new RoadEdge();
            if (!map.containsKey("weight"))
                return edge;
            g.setEdgeWeight(edge, Double.parseDouble(map.get("weight").getValue().replace(",", ".")));
            return edge;
        });
        importer.importGraph(g, new File(filename));

        return g;
    }

    public static void exportToDOTFile(String filename, Graph<Point, DefaultWeightedEdge> g) {
        DOTExporter<Point, DefaultWeightedEdge> export = new DOTExporter<>();
        export.setVertexIdProvider(Point::toString);
        export.setEdgeAttributeProvider((edge) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("weight", DefaultAttribute.createAttribute(edge.toString()));
            map.put("label", DefaultAttribute.createAttribute(edge.toString()));
            return map;
        });
        export.exportGraph(g, new File(filename));
    }

    public static void main(String[] args) {
        Graph<Point, DefaultWeightedEdge> g = RoadGraph.buildRandomGraph(20);
        System.out.println(getPathFromAtoB(g, "sem1", "sem10").getVertexList());

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Road Graph");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JGraphXAdapter<Point, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(g);

            mxIGraphLayout layout = new mxFastOrganicLayout(graphAdapter);
            layout.execute(graphAdapter.getDefaultParent());

            frame.add(new mxGraphComponent(graphAdapter));

            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });

        RoadGraph.exportToDOTFile("roadgraph.dot", g);
    }
}
