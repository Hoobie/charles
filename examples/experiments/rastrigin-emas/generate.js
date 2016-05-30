var generate = function(parameters){
	var number = parameters.number;
	var dimension = parameters.dimension;
	var range = parameters.range;
	function getRandomOfMaxAbs(maxAbs){
	 return (Math.random() - 0.5) * maxAbs * 2;
	}

	function getRandomInRange() {
	  return getRandomOfMaxAbs(range);
	}

	var result = [];
	for(var i =0; i<number; ++i){
		var coordinates = [];
		var individual = {energy: BASIC_ENERGY, coordinates:coordinates};
        individual.migrated = false;
		for(var j=0; j<dimension;++j){
			coordinates.push(getRandomInRange());
		}
		result.push(individual);
	}
    result.forEach(function (individual) {
        individual.fitness = calculateFitnessOfIndividual(individual);
    });
	return {individuals : result};
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
    var coordinates = individual.coordinates;
    return rastriginFunction(coordinates);
}
