var exec = require('cordova/exec');

exports.requestPermissions = function (success, error, params) {

    const { 
        customPermissions, 
        allVariables = false, 
        fitnessVariables = false, 
        healthVariables = false, 
        profileVariables = false, 
        groupPermissionsType = false 
    } = params;

    var args = [customPermissions, allVariables, fitnessVariables, healthVariables, profileVariables, groupPermissionsType];

    exec(success, error, 'OSHealthFitness', 'requestPermissions', args);
};

exports.getData = function (arg0, success, error) {
    exec(success, error, 'OSHealthFitness', 'getData', [arg0]);
};

exports.updateData = function (arg0, success, error) {
    exec(success, error, 'OSHealthFitness', 'updateData', [arg0]);
};

exports.enableBackgroundJob = function (arg0, success, error) {
    exec(success, error, 'OSHealthFitness', 'enableBackgroundJob', [arg0]);
};