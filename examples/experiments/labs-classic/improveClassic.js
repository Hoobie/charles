
function improve(population, options){
    var populationSize = population.individuals.length;
    var populationSizeAfterSelect = populationSize/2;
    for (var i = 0; i < options.iterations; ++i) {
        select(population, populationSizeAfterSelect);
        crossOver(population, populationSize);
        mutate(population);
        improvePopulationByLocalSearches(population)
    }
    population.individuals.forEach(function(individual){
        individual.fitness= fitness(individual)
    })
    return population;
}
