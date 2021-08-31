var exec = require('cordova/exec');

exports.initialize = function (success, error) {
    exec(success, error, 'HealthAndFitness', 'initialize');
};
