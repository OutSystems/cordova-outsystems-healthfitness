cordova.define("cordova-outsystems-health-fitness.cordova-outsystems-health-fitness", function(require, exports, module) {
    
    var exec = require('cordova/exec');

    exports.initialize = function (success, error) {
        exec(success, error, 'HealthAndFitness', 'initialize');
    };
        
    exports.getAgeSexAndBloodType = function (success, error) {
        exec(success, error, 'HealthAndFitness', 'getAgeSexAndBloodType');
    };

});
