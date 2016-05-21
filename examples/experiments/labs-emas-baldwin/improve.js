function improve(population, parameters) {
    iterations = parameters.iterations;
    individuals = population.individuals;

    individuals.forEach(function(individual) {
        individual.migrated = false;
    });

    populationSize = population.individuals.length;
    for (var i = 0; i < iterations; ++i) {
        meet(population);
        //mutate(population);
        calculateFitnesses(population);
    }
    return population;
}


function meet(population) {
    var newIndviduals = [];

    shuffle(population.individuals);
    population.individuals.forEach(function (individual) {
        for (var i in population.individuals) {
            var partner = population.individuals[i];
            if (Math.random() < MEET_PROBABILITY && partner != individual && partner.energy > 0 && individual.energy > 0) {
                if (individual.energy > CROSSOVER_MINIMUM_ENERGY && partner.energy > CROSSOVER_MINIMUM_ENERGY) {
                    newIndviduals.push(createChildren(individual, partner));
                    //var children = createChildren(individual, partner);
                    //newIndviduals.push(children[0]);
                    //newIndviduals.push(children[1]);
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
    population.individuals = population.individuals.concat(newIndviduals);
}

function fightIndividual(individualA, individualB) {
    if (individualA.fitness > individualB.fitness) {
        if (individualB.energy < ENERGY_EXCHANGE) {
            individualA.energy += individualB.energy;
            individualB.energy = 0;
        } else {
            individualA.energy += ENERGY_EXCHANGE;
            individualB.energy -= ENERGY_EXCHANGE;
        }
    } else if (individualB.fitness > individualA.fitness) {
        if (individualA.energy < ENERGY_EXCHANGE) {
            individualB.energy += individualA.energy;
            individualA.energy = 0;
        } else {
            individualB.energy += ENERGY_EXCHANGE;
            individualA.energy -= ENERGY_EXCHANGE;
        }
    }
}

{
    function calculateFitnesses(population) {
        population.individuals.forEach(function (individual) {
            individual.fitness = fitness(individual);
        });
    }

    function fitness(individual) {
        var length = individual.bytes.length;
        if (Math.random() < LOCAL_SEARCH_PROBABILITY) {
            return (length * length) / (2 * countEnergy(individual.bytes));
        } else {
            return (length * length) / (2 * countEnergy(baldwinLocalSearch(individual.bytes)));
        }
    }

    function baldwinLocalSearch(bytes) {
        var length = bytes.length;
        do {
            var originalEnergy = countEnergy(bytes);
            var bestBytes = bytes;
            var bestEnergy = originalEnergy;
            for (var i = 0; i < length; ++i) {
                var bytesCopy = bytes.slice();
                bytesCopy[i] = -bytesCopy[i];
                var newEnergy = countEnergy(bytesCopy);
                if (newEnergy < bestEnergy) {
                    bestEnergy = newEnergy;
                    bestBytes = bytesCopy;
                }
            }
            bytes = bestBytes;
        } while (bestEnergy < originalEnergy);
        return bytes;
    }

    function countEnergy(bytes) {
        var length = bytes.length;
        var sum = 0;
        for (var interval = 1; interval < length; ++interval) {
            var correl = countCorrelation(bytes, interval);
            sum += correl * correl;
        }
        return sum;
    }

    function countCorrelation(bytes, interval) {
        var length = bytes.length;
        var sum = 0;
        for (var i = 0; i < length - interval; ++i) {
            sum += bytes[i] * bytes[i + interval];
        }
        return sum;
    }
}


function createChildren(individualA, individualB) {
    var firstBytes = individualA.bytes.slice();
    var secondBytes = individualB.bytes.slice();
    individualA.energy -= NEWBORN_ENERGY / 2;
    individualB.energy -= NEWBORN_ENERGY / 2;
    var crossOverPoint = getRandomInt(0, firstBytes.length);
    var firstBytesEnding = firstBytes.splice(crossOverPoint);
    var secondBytesEnding = secondBytes.splice(crossOverPoint);
    return {bytes: firstBytes.concat(secondBytesEnding), energy: NEWBORN_ENERGY, migrated: false};
}


{
    function mutate(population) {
        population.individuals.forEach(mutateIndividual)
    }

    function mutateIndividual(individual) {
        if (Math.random() < INDIVIDUAL_MUTATION_PROBABILITY) {
            for (var i = 0; i < individual.bytes.length; ++i) {
                if (Math.random() < BIT_MUTATION_PROBABILITY) {
                    individual.bytes[i] = -individual.bytes[i];
                }
            }
        }
        return individual;
    }
}

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}

function shuffle(a) {
    var j, x, i;
    for (i = a.length; i; i -= 1) {
        j = Math.floor(Math.random() * i);
        x = a[i - 1];
        a[i - 1] = a[j];
        a[j] = x;
    }
}
