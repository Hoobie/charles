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
    })
    return {individuals : individuals};
};

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

