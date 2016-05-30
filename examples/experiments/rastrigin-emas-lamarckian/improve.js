function rastriginFunction(coordinates) {
    var n = coordinates.length;
    var A = 10;
    var result = A * n;
    for (var i = 0; i < n; ++i) {
        var xi = coordinates[i];
        result += xi * xi - A * Math.cos(2 * Math.PI * xi);
    }
    return result;
}

function calculateFitnessOfIndividual(individual) {
    var coordinates = individual.coordinates;
    return rastriginFunction(coordinates);
}

function improveByLocalSearch(population) {
    population.individuals.forEach(function (individual) {
        if (Math.random() < LOCAL_SEARCH_PROBABILITY) {
            individual.coordinates = localSearch(individual.coordinates);
        }
    })
}

function localSearch(coordinates) {
    var length = coordinates.length;
    do {
        var originalEnergy = rastriginFunction(coordinates);
        var bestBytes = coordinates;
        var bestEnergy = originalEnergy;
        for (var i = 0; i < length; ++i) {
            for (var sign = -1; sign < 2; sign += 2) {
                var bytesCopy = coordinates.slice();
                bytesCopy[i] = bytesCopy[i] + sign * LOCAL_SEARCH_RANGE;
                var newEnergy = rastriginFunction(bytesCopy);
                if (newEnergy < bestEnergy) {
                    bestEnergy = newEnergy;
                    bestBytes = bytesCopy;
                }
            }
        }
        coordinates = bestBytes;
    } while (bestEnergy < originalEnergy);
    return coordinates;
}


function calculateFitnesses(population) {
    population.individuals.forEach(function (individual) {
        individual.fitness = calculateFitnessOfIndividual(individual);
    });
}

function improve(population, parameters) {
    var iterations = parameters.iterations;
    var individuals = population.individuals;

    individuals.forEach(function (individual) {
        individual.migrated = false;
    });
    for (var i = 0; i < iterations; ++i) {
        meet(population);
        improveByLocalSearch(population);
        calculateFitnesses(population);
    }
    calculateFitnesses(population);
    return population;
}

function meet(population) {
    var newIndividuals = [];
    population.individuals.forEach(function (individual) {
        var individuals = population.individuals.filter(function (individual) {
            return individual.energy > 0;
        }).sort(function (a, b) {
            if (a.fitness < b.fitness) {
                return -1;
            } else if (a.fitness > b.fitness) {
                return 1;
            } else {
                return 0;
            }
        });
        for (var i in individuals) {
            var partner = individuals[i];
            if (Math.random() < MEET_PROBABILITY && partner != individual && partner.energy > 0 && individual.energy > 0) {
                if (individual.energy > CROSSOVER_MINIMUM_ENERGY && partner.energy > CROSSOVER_MINIMUM_ENERGY) {
                    newIndividuals.push(createChildren(individual, partner));
                } else {
                    fightIndividual(individual, partner)
                }
            }
            if (individual.energy <= 0) {
                return;
            }
        }
    });

    population.individuals = population.individuals.filter(function (individual) {
        return individual.energy > 0;
    });
    population.individuals = population.individuals.concat(newIndividuals);
}

function fightIndividual(individualA, individualB) {
    if (individualA.fitness < individualB.fitness) {
        if (individualB.energy < ENERGY_EXCHANGE) {
            individualA.energy += individualB.energy;
            individualB.energy = 0;
        } else {
            individualA.energy += ENERGY_EXCHANGE;
            individualB.energy -= ENERGY_EXCHANGE;
        }
    } else if (individualB.fitness < individualA.fitness) {
        if (individualA.energy < ENERGY_EXCHANGE) {
            individualB.energy += individualA.energy;
            individualA.energy = 0;
        } else {
            individualB.energy += ENERGY_EXCHANGE;
            individualA.energy -= ENERGY_EXCHANGE;
        }
    } else {
        if (Math.random() < 0.5) {
            if (individualB.energy < ENERGY_EXCHANGE) {
                individualA.energy += individualB.energy;
                individualB.energy = 0;
            } else {
                individualA.energy += ENERGY_EXCHANGE;
                individualB.energy -= ENERGY_EXCHANGE;
            }
        } else {
            if (individualA.energy < ENERGY_EXCHANGE) {
                individualB.energy += individualA.energy;
                individualA.energy = 0;
            } else {
                individualB.energy += ENERGY_EXCHANGE;
                individualA.energy -= ENERGY_EXCHANGE;
            }
        }
    }
}

{
    function mutate(population) {
        population.individuals = population.individuals.filter(function (individual) {
            return Math.random() < INDIVIDUAL_MUTATION_PROBABILITY && individual.energy > MUTATION_ENERGY;
        }).map(mutateIndividual).filter(function (a) {
            return a != null
        }).concat(population.individuals)
    }

    function mutateIndividual(individual) {
        var coordinates = [];
        for (var i = 0; i < individual.coordinates.length; ++i) {
            if (Math.random() < BIT_MUTATION_PROBABILITY) {
                var change = getRandomOfMaxAbs(MUTATION_MAX_VARIABLE_CHANGE);
                coordinates[i] = individual.coordinates[i] + change;
            } else {
                coordinates[i] = individual.coordinates[i];
            }
        }
        var newIndividual = {coordinates: coordinates, energy: MUTATION_ENERGY, migrated: false};
        newIndividual.fitness = calculateFitnessOfIndividual(newIndividual);
        if (newIndividual.fitness <= individual.fitness) {
            individual.energy -= MUTATION_ENERGY;
            return newIndividual;
        } else {
            return null;
        }
    }
}

function createChildren(individualA, individualB) {
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
    var firstIndividual = {coordinates: newCoordinatesA, energy: NEWBORN_ENERGY, migrated: false};
    var secondIndividual = {coordinates: newCoordinatesB, energy: NEWBORN_ENERGY, migrated: false};
    individualA.energy -= NEWBORN_ENERGY / 2;
    individualB.energy -= NEWBORN_ENERGY / 2;
    firstIndividual.fitness = calculateFitnessOfIndividual(firstIndividual);
    secondIndividual.fitness = calculateFitnessOfIndividual(secondIndividual);
    return firstIndividual.fitness < secondIndividual.fitness ? firstIndividual : secondIndividual;
}

function getRandomOfMaxAbs(maxAbs) {
    return (Math.random() - 0.5) * maxAbs * 2;
}

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}