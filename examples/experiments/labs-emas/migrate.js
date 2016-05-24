var migrate = function (firstPopulation, secondPopulation, parameters) {
    if (firstPopulation.individuals.length > 2) {
        firstPopulation.individuals.filter(function (individual) {
            return individual.energy >= MIGRATION_MINIMUM_ENERGY && Math.random() < MIGRATION_PROBABILITY && individual.migrated == false;
        }).forEach(function (individual) {
            var index = firstPopulation.individuals.indexOf(individual);
            var migrant = firstPopulation.individuals.splice(index, 1)[0];
            migrant.migrated = true;
            secondPopulation.individuals.push(migrant)
        });
    }

    if (secondPopulation.individuals.length > 2) {
        secondPopulation.individuals.filter(function (individual) {
            return individual.energy >= MIGRATION_MINIMUM_ENERGY && Math.random() < MIGRATION_PROBABILITY && individual.migrated == false;
        }).forEach(function (individual) {
            var index = secondPopulation.individuals.indexOf(individual);
            var migrant = secondPopulation.individuals.splice(index, 1)[0];
            migrant.migrated = true;
            firstPopulation.individuals.push(migrant)
        });
    }
    return {firstPopulation: firstPopulation, secondPopulation: secondPopulation};
};
