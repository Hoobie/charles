var fight = function(population){
    for(var i=0; i<FIGHTS_PER_ITERATION; ++i){
        var indexA = getRandomInt(0, population.individuals.length);
        var indexB = getRandomInt(0, population.individuals.length);
        var individualA = population.individuals[indexA];
        var individualB = population.individuals[indexB];
        if(individualA.fitness>individualB.fitness){
            individualA.energy+=ENERGY_EXCHANGE;
            individualB.energy-=ENERGY_EXCHANGE;
            if(individualB.energy==0){
                population.individuals.splice(indexB,1);
            }
        }else if(individualB.fitness>individualA.fitness){
            individualA.energy-=ENERGY_EXCHANGE;
            individualB.energy+=ENERGY_EXCHANGE;
            if(individualA.energy==0){
                population.individuals.splice(indexA,1);
            }
        }

    }
};
