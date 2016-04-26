
function improve(population, parameters){
    iterations = parameters.iterations;
    individuals = population.individuals;

    populationSize = population.individuals.length;
    for (var i = 0; i < iterations; ++i) {
        calculateFitnesses(population)
        fight(population)
        crossOver(population);
        improvePopulationByLocalSearches(population); // TODO comment if baldwin fitness will be used
    }
    calculateFitnesses(population)
    return population;
}