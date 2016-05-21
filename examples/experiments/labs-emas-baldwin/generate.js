var generate = function(parameters){
    var individualsCount = parameters.individualsCount;
    var bytesCount = parameters.bytesCount;
    var individuals = [];
    for(var i =0; i<individualsCount; ++i){
        var bytes = [];
        var individual = {bytes:bytes};
        for(var j=0; j<bytesCount;++j){
            bytes.push(Math.random()<0.5?-1:1);
        }
        individual.energy = BASIC_ENERGY;
        individual.migrated = false;
        individuals.push(individual);
    }

    individuals.forEach(function (individual) {
        individual.fitness = fitness(individual);
    });
    return {individuals : individuals};
};

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

