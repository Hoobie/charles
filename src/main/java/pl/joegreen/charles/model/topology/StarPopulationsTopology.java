package pl.joegreen.charles.model.topology;

import pl.joegreen.charles.model.Population;

import java.util.Set;

/**
 * Created by FleenMobile on 2016-05-22.
 */
public class StarPopulationsTopology extends PopulationsTopology {

    private Population populationInCenter = null;

    @Override
    public void add(Population population) {
        populationsGraph.addVertex(population);

        if (populationInCenter == null)
            populationInCenter = population;
        else {
            populationsGraph.addEdge(populationInCenter, population);
        }

    }

    @Override
    public void removeOne() {
        Set<Population> populations = populationsGraph.vertexSet();
        if (populations.size() > 0) {
            // Entire star has been removed
            if (populations.size() == 1)
                populationInCenter = null;
            populationsGraph.removeVertex(populations.iterator().next());
        }
    }
}
