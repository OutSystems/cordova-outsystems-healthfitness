var exec = require('cordova/exec');
exports.initialize = function (success, error) {
    exec(success, error, 'HealthAndFitness', 'initialize');
};
exports.getAgeSexAndBloodType = function (success, error) {
    exec(success, error, 'HealthAndFitness', 'getAgeSexAndBloodType');
};

exports.getHeartRates = function (success, error) {
    exec(success, error, 'HealthAndFitness', 'getHeartRates');
};
    