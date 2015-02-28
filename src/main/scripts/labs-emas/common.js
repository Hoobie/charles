function getRandomOfMaxAbs(maxAbs) {
    return (Math.random() - 0.5) * maxAbs * 2;
}

/*max is exclusive*/
function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}
