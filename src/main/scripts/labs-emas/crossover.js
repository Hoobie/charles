var mutateIndividual = function(individual){
    if (Math.random() < INDIVIDUAL_MUTATION_PROBABILITY) {
        for (var i = 0; i < individual.bytes.length; ++i) {
            if (Math.random() < BIT_MUTATION_PROBABILITY) {
                individual.bytes[i] = -individual.bytes[i];
            }
        }
    }
    return individual;
}
var createChildren = function (individualA, individualB) {
    var firstBytes = individualA.bytes.slice();
    var secondBytes = individualB.bytes.slice();
    individualA.energy-=BASIC_ENERGY;
    individualB.energy-=BASIC_ENERGY;
    var crossOverPoint = getRandomInt(0, firstBytes.length);
    var firstBytesEnding = firstBytes.splice(crossOverPoint);
    var secondBytesEnding = secondBytes.splice(crossOverPoint);
    firstBytes = firstBytes.concat(secondBytesEnding);
    secondBytes = secondBytes.concat(firstBytesEnding);
    return [
        mutateIndividual({bytes: firstBytes, energy: BASIC_ENERGY}),
        mutateIndividual({bytes: secondBytes, energy: BASIC_ENERGY})
    ];
};

var crossOver = function (population) {
    var individuals = population.individuals;
    var crossedOverIndividuals = [];
    for(var i = 0; i <CROSSOVER_ATTEMPTS_PER_ITERATION; ++i){
        var individualA = individuals[getRandomInt(0, individuals.length)];
        var individualB = individuals[getRandomInt(0, individuals.length)];
        if(individualA.energy<CROSSOVER_MINIMUM_ENERGY || individualB.energy<CROSSOVER_MINIMUM_ENERGY ||
            individualA === individualB){
            continue;
        }
        var children = createChildren(individualA, individualB);
        crossedOverIndividuals.push(children[0]);
        crossedOverIndividuals.push(children[1]);
    }

    population.individuals = population.individuals.concat(crossedOverIndividuals);

};