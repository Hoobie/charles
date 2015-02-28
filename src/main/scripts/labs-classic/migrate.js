var migrate = function (populations, parameters) {
    var numberOfPopulations = populations.length;
    var representatives = [];
    populations.forEach(function (population) {
        var populationRepresentatives = population.individuals.splice(0, numberOfPopulations);
        representatives.push(populationRepresentatives);
    });
    representatives.forEach(function (populationRepresentatives) {
        populationRepresentatives.forEach(function(individual, index){
            populations[index].individuals.push(individual);
        });
    });
    return populations;
};

