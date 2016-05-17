package pl.joegreen.charles.model.topology;

import pl.joegreen.charles.model.Population;

import java.util.LinkedList;

public class RingPopulationsTopology extends PopulationsTopology {

    @Override
    public void add(Population population) {
        LinkedList<Population> populations = new LinkedList<>(populationsGraph.vertexSet());
        populationsGraph.addVertex(population);
        if (populations.size() > 1) {
            populationsGraph.addEdge(populations.getLast(), population);
            populationsGraph.addEdge(populations.getFirst(), population);
        }
        if (populations.size() > 2) {
            populationsGraph.removeEdge(populations.getFirst(), populations.getLast());
        }
    }

    @Override
    public void removeOne() {
        LinkedList<Population> populations = new LinkedList<>(populationsGraph.vertexSet());
        if (populations.size() == 0) {
            return;
        }
        Population population = populations.getLast();
        populationsGraph.removeVertex(population);
        populations.removeLast();
        if (populations.size() > 2) {
            populationsGraph.addEdge(populations.getFirst(), populations.getLast());
        }
    }
}
