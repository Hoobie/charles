//var migrate = function (populations, parameters) {
var migrate = function (firstPopulation, secondPopulation, parameters) {
    var migrationCandidate = [];
    // get individuals that can migrate from each population

    populations = [firstPopulation, secondPopulation]
    populations.forEach(function (population) {
        population.individuals.filter(function (individual) {
            return individual.energy >= MIGRATION_MINIMUM_ENERGY;
        }).forEach(function (individual) {
            var index = population.individuals.indexOf(individual);
            migrationCandidate.push(population.individuals.splice(index, 1)[0])
        });
    })
    //firstPopulation.individuals.filter(function (individual) {
    //    return individual.energy >= MIGRATION_MINIMUM_ENERGY;
    //}).forEach(function (individual) {
    //    var index = firstPopulation.individuals.indexOf(individual);
    //    migrationCandidate.push(firstPopulation.individuals.splice(index, 1)[0])
    //});
    //
    //secondPopulation.individuals.filter(function (individual) {
    //    return individual.energy >= MIGRATION_MINIMUM_ENERGY;
    //}).forEach(function (individual) {
    //    var index = secondPopulation.individuals.indexOf(individual);
    //    migrationCandidate.push(secondPopulation.individuals.splice(index, 1)[0])
    //});

    migrationCandidate.forEach(function (individual) {
        if (Math.random() > 0.5) {
            firstPopulation.individuals.push(individual)
        } else {
            secondPopulation.individuals.push(individual)
        }
    })
    return {firstPopulation: firstPopulation, secondPopulation: secondPopulation};
};
