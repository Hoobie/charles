var migrate = function (firstPopulation, secondPopulation, parameters) {
    for (var i = 0; i < parameters.toMigrate; i++) {
        var firstIndex = Math.floor((Math.random() * firstPopulation.individuals.length))
        var secondIndex = Math.floor((Math.random() * secondPopulation.individuals.length))

        var temp = firstPopulation.individuals[i];
        firstPopulation.individuals[i] = secondPopulation.individuals[i];
        secondPopulation.individuals[i] = temp;
    }

    return {firstPopulation: firstPopulation, secondPopulation: secondPopulation};
};

