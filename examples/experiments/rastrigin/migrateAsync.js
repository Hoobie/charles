/*max is exclusive*/
function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}


function copyIndividual(individual){
    return {energy: individual.energy, coordinates: individual.coordinates.slice()}
}

function copyIndividuals(individualsArray){
    return individualsArray.map(function(individual){
        return copyIndividual(individual);
    })
}

var migrate = function (population, migrationPool, parameters) {
    var migrationPoolIndividuals = migrationPool.individuals;
    var populationIndividualsCopy = copyIndividuals(population.individuals);
    var migrantsPerMetaIteration = parameters.migrantsPerMetaIteration;
    var toEmigrate = [];
    var migrantsNumber = Math.min(populationIndividualsCopy.length, migrantsPerMetaIteration);
    for(var i =0; i<migrantsNumber; ++i){
        var indexToEmigrate = getRandomInt(0, populationIndividualsCopy.length);
        var emigratingCopy = copyIndividual(populationIndividualsCopy.splice(indexToEmigrate,1)[0]);
        emigratingCopy.metaIteration = population.metaIteration;
        toEmigrate.push(emigratingCopy)
    }


    var migrationPoolIndividualsWithSimilarMetaIteration = migrationPoolIndividuals.filter(function(individual){
       return  Math.abs(individual.metaIteration - population.metaIteration) <=1
    })


    if(migrationPoolIndividualsWithSimilarMetaIteration.length>=migrantsNumber){
        var toImigrate = copyIndividuals(migrationPoolIndividualsWithSimilarMetaIteration.slice(0, migrantsNumber));
        migrationPool.individuals = migrationPoolIndividuals.concat(toEmigrate);
        population.individuals = populationIndividualsCopy.concat(toImigrate);
    }else{
        migrationPool.individuals = migrationPoolIndividuals.concat(toEmigrate);
    }

    return {population: population, migrationPool: migrationPool};
};

