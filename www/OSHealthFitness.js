var exec = require('cordova/exec');
exports.initialize = function (success, error) {
    exec(success, error, 'HealthAndFitness', 'initialize');
};
exports.getAgeSexAndBloodType = function (success, error) {
    exec(success, error, 'HealthAndFitness', 'getAgeSexAndBloodType');
};

exports.saveSteps = function (success, error) {
    exec(success, error, 'HealthAndFitness', 'saveSteps');
};

exports.getHeartRates = function (success, error) {
    exec(success, error, 'HealthAndFitness', 'getHeartRates');
};

exports.loadMostRecentWeight = function (success, error) {
        exec(success, error, 'HealthAndFitness', 'loadMostRecentWeight');
};
    
exports.loadMostRecentHeight = function (success, error) {
        exec(success, error, 'HealthAndFitness', 'loadMostRecentHeight');
};