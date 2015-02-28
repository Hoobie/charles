var iterations;
var population;
var individuals;

var dimension;
var populationSize;

var mutationMaxVariableChange = 1;
var mutationVariableChangeProbability = 0.2;

var BASIC_ENERGY = 30;
var FIGHTS_PER_ITERATION = 1000;
var ENERGY_EXCHANGE = 1;
var CROSSOVER_ATTEMPTS_PER_ITERATION = 1000;
var CROSSOVER_MINIMUM_ENERGY = 40;


var rastriginFunction = function () {
    var n = arguments.length;
    var A = 10;
    var result = A * n;
    for (var i = 0; i < n; ++i) {
        var xi = arguments[i];
        result += xi * xi - A * Math.cos(2 * Math.PI * xi);
    }
    return result;
}


function getRandomOfMaxAbs(maxAbs) {
    return (Math.random() - 0.5) * maxAbs * 2;
}


/*max is exclusive*/
function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}


var calculateFitnessOfIndividual = function (individual) {
    var coordinates = individual.coordinates;
    var fitness = 1.0 / rastriginFunction.apply(this, coordinates);

    return fitness;
}



var setBasicEnergyLevels = function (population){
    population.individuals.forEach(function(individual){
        individual.energy = BASIC_ENERGY;
    });

};

var calculateFitnesses = function(population){
    population.individuals.forEach(function(individual){
        individual.fitness = calculateFitnessOfIndividual(individual);
    });
}


var fight = function(population){
    for(var i=0; i<FIGHTS_PER_ITERATION; ++i){
        var indexA = getRandomInt(0, population.individuals.length);
        var indexB = getRandomInt(0, population.individuals.length);
        var individualA = population.individuals[indexA];
        var individualB = population.individuals[indexB];
        if(individualA.fitness>individualB.fitness){
            individualA.energy+=ENERGY_EXCHANGE;
            individualB.energy-=ENERGY_EXCHANGE;
            if(individualB.energy==0){
                population.individuals.splice(indexB,1);
            }
        }else if(individualB.fitness>individualA.fitness){
            individualA.energy-=ENERGY_EXCHANGE;
            individualB.energy+=ENERGY_EXCHANGE;
            if(individualA.energy==0){
               population.individuals.splice(indexA,1);
            }
        }

    }
}



var crossOver = function (population) {
    var individuals = population.individuals;


	var mutateIndividual = function(individual){
		for (var i = 0; i < individual.coordinates.length; ++i) {
			if (Math.random() < mutationVariableChangeProbability) {
				var change = getRandomOfMaxAbs(mutationMaxVariableChange);
				individual.coordinates[i] += change;
			}
		}
		return individual;
	}
    var createChildren = function (individualA, individualB) {
        var coordinatesA = individualA.coordinates;
        var coordinatesB = individualB.coordinates;
        var indexToStartCrossover = getRandomInt(0, coordinatesA.length);
        var newCoordinatesA = coordinatesA.slice(0);
        var newCoordinatesB = coordinatesB.slice(0);
        for (var indexToCrossOver = indexToStartCrossover; indexToCrossOver < coordinatesA.length; ++indexToCrossOver) {
            var alpha = Math.random();
            var valueA = coordinatesA[indexToCrossOver];
            var valueB = coordinatesB[indexToCrossOver];

            var newValueA = valueA - alpha * (valueA - valueB);
            var newValueB = valueB + alpha * (valueA - valueB);


            newCoordinatesA[indexToCrossOver] = newValueA;

            newCoordinatesB[indexToCrossOver] = newValueB;
        }
        individualA.energy -= BASIC_ENERGY;
        individualB.energy -= BASIC_ENERGY;
        return [
            mutateIndividual({coordinates: newCoordinatesA, energy: BASIC_ENERGY}),
            mutateIndividual({coordinates: newCoordinatesB, energy: BASIC_ENERGY})
        ];
    }


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

}



var printPopulation = function(population){
 console.log("---Population---")
 var energySum = 0;
 population.individuals.forEach(function(individual){
	console.log(individual.coordinates, individual.fitness, individual.energy);
	energySum+=individual.energy;
 });
 console.log("EnergySum:", energySum);
 console.log("---/---")

}

function improve(population, parameters){
	iterations = parameters.iterations;
    individuals = population.individuals;
	dimension = population.individuals[0].coordinates.length;

    populationSize = population.individuals.length;
    //setBasicEnergyLevels(population)
    for (var i = 0; i < iterations; ++i) {
        fight(population)
        calculateFitnesses(population)
        crossOver(population);
    }
    calculateFitnesses(population)
    return population;
}
