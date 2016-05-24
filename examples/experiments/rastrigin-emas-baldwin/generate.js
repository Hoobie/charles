var generate = function (parameters) {
    var number = parameters.number;
    var dimension = parameters.dimension;
    var range = RANGE;

    function getRandomOfMaxAbs(maxAbs) {
        return (Math.random() - 0.5) * maxAbs * 2;
    }

    function getRandomInRange() {
        return getRandomOfMaxAbs(range);
    }

    var result = [];
    for (var i = 0; i < number; ++i) {
        var coordinates = [];
        var individual = {energy: BASIC_ENERGY, coordinates: coordinates};
        individual.migrated = false;
        individual.localSearch = Math.random() < LOCAL_SEARCH_PROBABILITY;
        for (var j = 0; j < dimension; ++j) {
            coordinates.push(getRandomInRange());
        }
        result.push(individual);
    }
    result.forEach(function (individual) {
        individual.fitness = calculateFitnessOfIndividual(individual);
    });
    return {individuals: result};
};

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
    if (individual.localSearch) {
        var coordinates = baldwinLocalSearch(individual.coordinates);
        return rastriginFunction(coordinates);
    } else {
        var coordinates = individual.coordinates;
        return rastriginFunction(coordinates);
    }
}

function baldwinLocalSearch(coordinates) {
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
