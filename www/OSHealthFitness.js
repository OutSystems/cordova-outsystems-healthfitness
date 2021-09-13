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

exports.getData = function (success, error, params) {
    exec(success, error, 'OSHealthFitness', 'getData', [params]);
};

exports.updateData = function (success, error) {
    exec(success, error, 'OSHealthFitness', 'updateData');
};

exports.enableBackgroundJob = function (success, error) {
    exec(success, error, 'OSHealthFitness', 'enableBackgroundJob');
};

exports.writeData = function (variable, value, success, error) {
    exec(success, error, 'OSHealthFitness', 'writeData', [variable,value]);
};
