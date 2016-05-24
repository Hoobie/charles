var migrate = function (firstPopulation, secondPopulation, parameters) {

    if (firstPopulation.individuals.length > 2) {
        firstPopulation.individuals = firstPopulation.individuals.sort(function (a, b) {
            if (a.fitness < b.fitness) {
                return -1;
            } else if (a.fitness > b.fitness) {
                return 1;
            } else {
                return 0;
            }
        });
        firstPopulation.individuals.filter(function (individual) {
            return individual.energy >= MIGRATION_MINIMUM_ENERGY && Math.random() < MIGRATION_PROBABILITY && individual.migrated == false
                && firstPopulation.individuals.indexOf(individual) > 2;
        }).forEach(function (individual) {
            var index = firstPopulation.individuals.indexOf(individual);
            var migrant = firstPopulation.individuals.splice(index, 1)[0];
            migrant.migrated = true;
            secondPopulation.individuals.push(migrant)
        });
    }

    if (secondPopulation.individuals.length > 2) {
        secondPopulation.individuals = secondPopulation.individuals.sort(function (a, b) {
            if (a.fitness < b.fitness) {
                return -1;
            } else if (a.fitness > b.fitness) {
                return 1;
            } else {
                return 0;
            }
        });

        secondPopulation.individuals.filter(function (individual) {
            return individual.energy >= MIGRATION_MINIMUM_ENERGY && Math.random() < MIGRATION_PROBABILITY && individual.migrated == false
                && secondPopulation.individuals.indexOf(individual) > 2;
        }).forEach(function (individual) {
            var index = secondPopulation.individuals.indexOf(individual);
            var migrant = secondPopulation.individuals.splice(index, 1)[0];
            migrant.migrated = true;
            firstPopulation.individuals.push(migrant)
        });
    }
    return {firstPopulation: firstPopulation, secondPopulation: secondPopulation};
};
