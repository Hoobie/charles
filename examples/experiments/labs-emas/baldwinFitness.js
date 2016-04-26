function countCorrelation(bytes, interval) {
    var length = bytes.length;
    var sum = 0;
    for (var i = 0; i < length - interval; ++i) {
        sum += bytes[i] * bytes[i + interval];
    }
    return sum;
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

function baldwinLocalSearch(bytes) {
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

function fitness(individual) {
    var length = individual.bytes.length;
    return (length * length) / (2 * countEnergy(baldwinLocalSearch(individual.bytes)));
}

var calculateFitnesses = function (population) {
    population.individuals.forEach(function (individual) {
        individual.fitness = fitness(individual);
    });
}
