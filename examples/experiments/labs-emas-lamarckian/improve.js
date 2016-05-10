function improve(population, parameters) {
    iterations = parameters.iterations;
    individuals = population.individuals;

    populationSize = population.individuals.length;
    for (var i = 0; i < iterations; ++i) {
        calculateFitnesses(population);
        fight(population);
        crossOver(population);
    }
    calculateFitnesses(population);
    return population;
}

{
    function calculateFitnesses(population) {
        population.individuals.forEach(function (individual) {
            individual.fitness = fitness(individual);
        });
    }

    function fitness(individual) {
        var length = individual.bytes.length;
        return (length * length) / (2 * countEnergy(individual.bytes));
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

function fight(population) {
    for (var i = 0; i < FIGHTS_PER_ITERATION; ++i) {
        var indexA = getRandomInt(0, population.individuals.length);
        var indexB = getRandomInt(0, population.individuals.length);
        var individualA = population.individuals[indexA];
        var individualB = population.individuals[indexB];
        if (individualA.fitness > individualB.fitness) {
            individualA.energy += ENERGY_EXCHANGE;
            individualB.energy -= ENERGY_EXCHANGE;
            if (individualB.energy == 0) {
                population.individuals.splice(indexB, 1);
            }
        } else if (individualB.fitness > individualA.fitness) {
            individualA.energy -= ENERGY_EXCHANGE;
            individualB.energy += ENERGY_EXCHANGE;
            if (individualA.energy == 0) {
                population.individuals.splice(indexA, 1);
            }
        }

    }
}

{
    function crossOver(population) {
        var crossedOverIndividuals = [];

        var crossoverCandidates = population.individuals.filter(function (individual) {
            return individual.energy > CROSSOVER_MINIMUM_ENERGY;
        });
        if (crossoverCandidates.length < 2) {
            return;
        }
        shuffle(crossoverCandidates);
        crossoverCandidates.forEach(function (candidate) {
            crossoverCandidates.filter(function (individual) {
                return Math.random() <= CROSSOVER_PROBABILITY && individual.energy > CROSSOVER_MINIMUM_ENERGY && candidate != individual;
            }).forEach(function (individual) {
                if (candidate.energy < CROSSOVER_MINIMUM_ENERGY) {
                    return;
                }
                var children = createChildren(candidate, individual);
                crossedOverIndividuals.push(children[0]);
                crossedOverIndividuals.push(children[1]);
            });
        });
        population.individuals = population.individuals.concat(crossedOverIndividuals);
    }

    function createChildren(individualA, individualB) {
        var firstBytes = individualA.bytes.slice();
        var secondBytes = individualB.bytes.slice();
        individualA.energy -= BASIC_ENERGY;
        individualB.energy -= BASIC_ENERGY;
        var crossOverPoint = getRandomInt(0, firstBytes.length);
        var firstBytesEnding = firstBytes.splice(crossOverPoint);
        var secondBytesEnding = secondBytes.splice(crossOverPoint);
        firstBytes = firstBytes.concat(secondBytesEnding);
        secondBytes = secondBytes.concat(firstBytesEnding);
        return [
            {bytes: localSearch(firstBytes), energy: BASIC_ENERGY},
            {bytes: localSearch(secondBytes), energy: BASIC_ENERGY}
        ];
    }

    function localSearch(bytes) {
        var length = bytes.length;
        //TODO: efficiency!
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
