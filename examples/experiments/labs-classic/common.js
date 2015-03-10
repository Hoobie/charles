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


function fitness(individual){
    var length = individual.bytes.length;
    return (length*length)/(2*countEnergy(individual.bytes));
}



function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}
