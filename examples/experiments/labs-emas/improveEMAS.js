
function improve(population, parameters){
    iterations = parameters.iterations;
    individuals = population.individuals;

    populationSize = population.individuals.length;
    for (var i = 0; i < iterations; ++i) {
        calculateFitnesses(population)
        fight(population)
        crossOver(population);
        improvePopulationByLocalSearches(population);
    }
    calculateFitnesses(population)
    return population;
}