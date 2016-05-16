package pl.joegreen.charles.model.topology;

import pl.joegreen.charles.model.Population;

import java.util.Set;

public class FullMeshPopulationsTopology extends PopulationsTopology {

    @Override
    public void add(Population population) {
        populationsGraph.addVertex(population);
        Set<Population> populations = populationsGraph.vertexSet();
        for (Population prev : populations) {
            populationsGraph.addEdge(prev, population);
        }
    }

    @Override
    public void removeOne() {
        Set<Population> populations = populationsGraph.vertexSet();
        if (populations.size() > 0) {
            populationsGraph.removeVertex(populations.iterator().next());
        }
    }
}
