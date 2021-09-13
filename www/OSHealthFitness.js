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

exports.queryData = function (success, error, params) {
    
    const startDate = params[0];
    const endDate = params[1];
    const type = params[2];
    
    var args = [startDate, endDate, type];
    
    exec(success, error, 'OSHealthFitness', 'queryData', args);
};