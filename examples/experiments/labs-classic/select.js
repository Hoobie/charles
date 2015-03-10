var select = function (population, populationSizeAfterSelect) {
    var individuals = population.individuals;
    var populationSize = individuals.length;
    var totalFitness = 0;
    individuals.forEach(function (individual, index) {
        individual.fitness = fitness(individual);
        totalFitness += individual.fitness;
    });

    individuals = individuals.sort(function (a, b) {
        return b.fitness - a.fitness;
    });

    var scaledFitnesses = individuals.map(function (individual) {
        return individual.fitness / totalFitness;
    });

    individuals.forEach(function (individual, index) {
        delete individual.fitness
    });

    var cumulativeScaledFitnesses = [];
    scaledFitnesses.forEach(function (fitness, index) {
        if (index > 0) {
            cumulativeScaledFitnesses[index] = cumulativeScaledFitnesses[index - 1] + fitness;
        } else {
            cumulativeScaledFitnesses[0] = fitness;
        }
    });

    var selectIndividual = function () {
        var rand = Math.random();

        for (var index = 0; index < cumulativeScaledFitnesses.length; ++index) {
            var cumulativeScaledFitness = cumulativeScaledFitnesses[index];
            if (cumulativeScaledFitness > rand) {
                return individuals[index];
            }
        }
    }

    var newIndividuals = [individuals[0]];
    while (newIndividuals.length < populationSizeAfterSelect) {
        newIndividuals.push(selectIndividual());
    }
    population.individuals = newIndividuals;
}
