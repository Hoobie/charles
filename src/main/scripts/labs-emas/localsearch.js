

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

function improvePopulationByLocalSearches(population) {
    population.individuals.forEach(function (individual) {
        individual.bytes = localSearch(individual.bytes) //TODO: mark if bytes were changed after last search
    });
    return population;
}