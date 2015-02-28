function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}

var createChildren = function (firstIndividual, secondIndividual) {
    var firstBytes = firstIndividual.bytes.slice();
    var secondBytes = secondIndividual.bytes.slice();
    var crossOverPoint = getRandomInt(0, firstBytes.length);
    var firstBytesEnding = firstBytes.splice(crossOverPoint);
    var secondBytesEnding = secondBytes.splice(crossOverPoint);
    firstBytes = firstBytes.concat(secondBytesEnding);
    secondBytes = secondBytes.concat(firstBytesEnding);
    return [{bytes: firstBytes}, {bytes: secondBytes}];
};
var crossOver = function (population, populationSizeAfterCrossover) {
    var individuals = population.individuals;
    var crossedOverIndividuals = [];
    while (crossedOverIndividuals.length + individuals.length < populationSizeAfterCrossover) {
        var individualA = individuals[getRandomInt(0, individuals.length)];
        var individualB = individuals[getRandomInt(0, individuals.length)];
        var children = createChildren(individualA, individualB);
        crossedOverIndividuals.push(children[0]);
        crossedOverIndividuals.push(children[1]);
    }
    population.individuals = population.individuals.concat(crossedOverIndividuals);
}
