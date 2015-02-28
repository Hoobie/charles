var migrate = function (populations, parameters) {
    var numberOfPopulations = populations.length;
    var representatives = [];
    // get individuals that can migrate from each population
    populations.forEach(function (population) {
        population.individuals.splice(0, numberOfPopulations).filter(function(individual){
           return individual.energy>=MIGRATION_MINIMUM_ENERGY;
        }).forEach(function(individual){
            representatives.push(individual);
        })
    });

    var toMigrate = Math.min(MAX_MIGRANTS, representatives.length);

    populations.forEach(function(population){
        population.individuals.sort(function(individualA, individualB){
            return individualB.energy - individualA.energy;
        });

        population.individuals.splice(0, toMigrate);

        for(var i = 0; i<toMigrate; ++i){
            var individualToCOpy = representatives[getRandomInt(0, toMigrate)];
            population.individuals.push({energy:individualToCOpy.energy, bytes:individualToCOpy.bytes.slice()});
        }
    });

    return populations;
};

//var migrate = function (populations, parameters) {
//    var numberOfPopulations = populations.length;
//    var representatives = [];
//    populations.forEach(function (population) {
//        var populationRepresentatives = population.individuals.splice(0, numberOfPopulations);
//        representatives.push(populationRepresentatives);
//    });
//    representatives.forEach(function (populationRepresentatives) {
//        populationRepresentatives.forEach(function(individual, index){
//            populations[index].individuals.push({energy: individual.energy, fitness: individual.fitness, bytes:individual.bytes.slice()});
//        });
//    });
//    return populations;
//};

