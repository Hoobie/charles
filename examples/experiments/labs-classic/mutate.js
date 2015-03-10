var mutate = function (population) {
    population.individuals.forEach(function (individual) {
        if (Math.random() < 0.1) {
            for (var i = 0; i < individual.bytes.length; ++i) {
                if (Math.random() < 0.2) {
                    individual.bytes[i] = -individual.bytes[i];
                }
            }
        }
    });
    return population;
}
