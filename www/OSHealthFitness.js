var exec = require('cordova/exec');

exports.requestPermissions = function (success, error, params) {

    const { 
        customPermissions, 
        allVariables,
        fitnessVariables,
        healthVariables,
        profileVariables,
        summaryVariables
    } = params;

    var args = [customPermissions, allVariables, fitnessVariables, healthVariables, profileVariables, summaryVariables];
    exec(success, error, 'OSHealthFitness', 'requestPermissions', args);
};

exports.getData = function (success, error) {
    exec(success, error, 'OSHealthFitness', 'getData');
};