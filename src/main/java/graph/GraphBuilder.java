package graph;

import com.mxgraph.layout.*;
import com.mxgraph.swing.mxGraphComponent;
import org.jgrapht.Graph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GraphBuilder {
    private GraphBuilder() {
        // Ensure non-instantiability
    }

    public static Graph<String, DefaultEdge> buildGridGraph(int size) {
        Graph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<String> v = new ArrayList<>();

        for (int i = 0; i < size * size; i++) {
            graph.addVertex(String.format("v%d", i+1));
            v.add(String.format("v%d", i+1));
        }

        for (int i = 0; i < size * size; i += size) {
            for (int j = 0; j < size - 1; j++) {
                graph.addEdge(v.get(i + j), v.get(i + j + 1));
            }
        }

        for (int i = 0; i < size * size - size; i += size) {
            for (int j = 0; j < size; j++) {
                graph.addEdge(v.get(i+j), v.get(i+j+size));
            }
        }

        return graph;
    }

    private static void showGui(Graph<String, DefaultEdge> g) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Graph");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(g);

            mxIGraphLayout layout = new mxFastOrganicLayout(graphAdapter);
            layout.execute(graphAdapter.getDefaultParent());

            frame.add(new mxGraphComponent(graphAdapter));

            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        Graph<String, DefaultEdge> graph = GraphBuilder.buildGridGraph(7);

        DijkstraShortestPath<String, DefaultEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);

        ShortestPathAlgorithm.SingleSourcePaths<String, DefaultEdge> iPaths = dijkstraAlg.getPaths("v1");

        System.out.println(iPaths.getPath("v49").getVertexList() + "\n");

        showGui(graph);
    }
}
