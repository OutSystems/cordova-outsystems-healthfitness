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
