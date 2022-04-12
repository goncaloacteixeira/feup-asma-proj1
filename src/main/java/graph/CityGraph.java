package graph;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.dot.DOTImporter;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class CityGraph {
    private CityGraph() {}

    public static void exportToDOTFile(String filename, Graph<Point, DefaultWeightedEdge> g) {
        DOTExporter<Point, DefaultWeightedEdge> export = new DOTExporter<>();
        export.setVertexIdProvider(Point::toString);
        export.setVertexAttributeProvider((vertex) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("type", DefaultAttribute.createAttribute(vertex.getClass().getSimpleName().toLowerCase(Locale.ROOT)));
            return map;
        });

        export.setEdgeAttributeProvider((edge) -> {
            Map<String, Attribute> map = new LinkedHashMap<>();
            map.put("weight", DefaultAttribute.createAttribute(g.getEdgeWeight(edge)));
            map.put("label", DefaultAttribute.createAttribute(edge.toString()));
            map.put("type", DefaultAttribute.createAttribute(((Edge) edge).getType()));
            return map;
        });
        export.exportGraph(g, new File(filename));
    }

    public static Graph<Point, DefaultWeightedEdge> getFromDOTFile(String filename) {
        DOTImporter<Point, DefaultWeightedEdge> importer = new DOTImporter<>();
        Graph<Point, DefaultWeightedEdge> g = new WeightedMultigraph<>(DefaultWeightedEdge.class);

        importer.setVertexWithAttributesFactory((vertex, map) -> {
            Point point = new Point("s");

            if (map.containsKey("type")) {
                switch (map.get("type").getValue()) {
                    case "point" -> point = new Point(vertex);
                    case "station" -> point = new Station(vertex);
                    case "semaphore" -> point = new Semaphore(vertex);
                    default -> {}
                }
            }

            return point;
        });

        importer.setEdgeWithAttributesFactory((map) -> {
            DefaultWeightedEdge edge = new RoadEdge();

            if (map.containsKey("type")) {
                switch (map.get("type").getValue()) {
                    case "road" -> edge = new RoadEdge();
                    case "subway" -> edge = new SubwayEdge();
                    case "street" -> edge = new StreetEdge();
                    default -> {}
                }
            }

            if (map.containsKey("weight"))
                g.setEdgeWeight(edge, Double.parseDouble(map.get("weight").getValue().replace(",", ".")));

            return edge;
        });
        importer.importGraph(g, new File(filename));

        return g;
    }

    public static GraphPath<Point, DefaultWeightedEdge> getPathFromAtoB(Graph<Point, DefaultWeightedEdge> graph, String a, String b) {
        DijkstraShortestPath<Point, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath<>(graph);
        ShortestPathAlgorithm.SingleSourcePaths<Point, DefaultWeightedEdge> iPaths = dijkstraAlg.getPaths(Point.instance(a));

        return iPaths.getPath(Point.instance(b));
    }

    public static Graph<Point, DefaultWeightedEdge> importWithNoRoads(String filename) {
        Graph<Point, DefaultWeightedEdge> graph = CityGraph.getFromDOTFile(filename);
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            if (edge instanceof RoadEdge)
                graph.setEdgeWeight(edge, Integer.MAX_VALUE);
        }
        return graph;
    }

    public static String printPath(Graph<Point, DefaultWeightedEdge> graph, GraphPath<Point, DefaultWeightedEdge> path) {
        StringBuilder result = new StringBuilder();
        for (DefaultWeightedEdge edge : path.getEdgeList()) {
            result.append(String.format("%s --[%s]--> ", graph.getEdgeSource(edge), edge));
        }
        result.append(graph.getEdgeTarget(path.getEdgeList().get(path.getEdgeList().size() - 1)).toString());

        result.append(" -- COST: ").append(path.getWeight());

        return result.toString();
    }

    public static void main(String[] args) {
        Graph<Point, DefaultWeightedEdge> graph = getFromDOTFile("citygraph.dot");
        GraphPath<Point, DefaultWeightedEdge> path = getPathFromAtoB(graph, "sem1", "sta1");
        System.out.println(printPath(graph, path));

        graph = importWithNoRoads("citygraph.dot");
        path = getPathFromAtoB(graph, "sem1", "sta1");
        System.out.println(printPath(graph, path));
    }
}
