var BASIC_ENERGY = 30;


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
		for(var j=0; j<dimension;++j){
			coordinates.push(getRandomInRange());
		}
		result.push(individual);
	}
	return {individuals : result};
}
