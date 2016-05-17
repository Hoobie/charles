package pl.joegreen.charles.model.topology;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import pl.joegreen.charles.model.Population;
import pl.joegreen.charles.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class PopulationsTopology {
    // need to be directed to allow loops
    protected Graph<Population, DefaultEdge> populationsGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

    public abstract void add(Population population);

    public abstract void removeOne();

    public List<Pair<Population, Population>> getPairs() {
        return populationsGraph.edgeSet().stream()
                .map(e -> new Pair<>(populationsGraph.getEdgeSource(e), populationsGraph.getEdgeTarget(e)))
                .collect(Collectors.toList());
    }

    public List<Population> asList() {
        return new ArrayList<>(populationsGraph.vertexSet());
    }

    public int size() {
        return populationsGraph.vertexSet().size();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int i = 0;
        for (Population population : populationsGraph.vertexSet()) {
            builder.append("--- Population ")
                    .append(++i)
                    .append(" ---")
                    .append(JsonUtil.toPrettyJsonString(population.getMapRepresentation()));
        }
        return builder.toString();
    }
}
