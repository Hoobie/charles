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
        individuals.push(individual);
    }
    return {individuals : individuals};
}
