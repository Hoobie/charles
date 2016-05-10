var migrate = function (firstPopulation, secondPopulation, parameters) {
    for (i = 0; i < parameters.toMigrate; i++) {
        var firstIndex = Math.floor((Math.random() * firstPopulation.length))
        var secondIndex = Math.floor((Math.random() * secondPopulation.length))

        var temp = firstPopulation[firstIndex];
        firstPopulation[firstIndex] = secondPopulation[secondIndex];
        secondPopulation[secondIndex] = temp;
    }

    return {firstPopulation: firstPopulation, secondPopulation: secondPopulation};
};

