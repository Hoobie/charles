var migrate = function (firstPopulation, secondPopulation, parameters) {
    var temp = firstPopulation[0];
    firstPopulation[0] = secondPopulation[0];
    secondPopulation[0] = temp;

    return {firstPopulation: firstPopulation, secondPopulation: secondPopulation};
};

