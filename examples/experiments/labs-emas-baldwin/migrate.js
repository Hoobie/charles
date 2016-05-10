var migrate = function (firstPopulation, secondPopulation, parameters) {
    var migrationCandidate = [];

    populations = [firstPopulation, secondPopulation]
    populations.forEach(function (population) {
        population.individuals.filter(function (individual) {
            return individual.energy >= MIGRATION_MINIMUM_ENERGY;
        }).forEach(function (individual) {
            var index = population.individuals.indexOf(individual);
            migrationCandidate.push(population.individuals.splice(index, 1)[0])
        });
    });

    migrationCandidate.forEach(function (individual) {
        if (Math.random() > 0.5) {
            firstPopulation.individuals.push(individual)
        } else {
            secondPopulation.individuals.push(individual)
        }
    })
    return {firstPopulation: firstPopulation, secondPopulation: secondPopulation};
};
