const fs = require('fs');
const path = require('path');
const { ConfigParser } = require('cordova-common');
const et = require('elementtree');
const { DOMParser, XMLSerializer } = require('xmldom');
const { captureRejectionSymbol } = require('events');

module.exports = async function (context) {
    const projectRoot = context.opts.cordova.project ? context.opts.cordova.project.root : context.opts.projectRoot;

    const configXML = path.join(projectRoot, 'config.xml');
    const configParser = new ConfigParser(configXML);

    console.log('About to call addPermissionsToManfiest');
        
    addPermissionsToManifest(configParser, projectRoot);


};

function addPermissionsToManifest(configParser, projectRoot) {

    // Permission groups
    const fitnessPermissionsRead = [
        { name: 'android.permission.health.READ_STEPS' },
        { name: 'android.permission.health.READ_TOTAL_CALORIES_BURNED' },
        { name: 'android.permission.health.READ_SPEED' },
        { name: 'android.permission.health.READ_DISTANCE' }
    ];

    const fitnessPermissionsWrite = [
        { name: 'android.permission.health.WRITE_STEPS' },
        { name: 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED' },
        { name: 'android.permission.health.WRITE_SPEED' },
        { name: 'android.permission.health.WRITE_DISTANCE' }
    ];

    const healthPermissionsRead = [
        { name: 'android.permission.health.READ_HEART_RATE' },
        { name: 'android.permission.health.READ_SLEEP' },
        { name: 'android.permission.health.READ_BLOOD_PRESSURE' },
        { name: 'android.permission.health.READ_BLOOD_GLUCOSE' }
    ];

    const healthPermissionsWrite = [
        { name: 'android.permission.health.WRITE_HEART_RATE' },
        { name: 'android.permission.health.WRITE_SLEEP' },
        { name: 'android.permission.health.WRITE_BLOOD_PRESSURE' },
        { name: 'android.permission.health.WRITE_BLOOD_GLUCOSE' }
    ];

    const profilePermissionsRead = [
        { name: 'android.permission.health.READ_WEIGHT' },
        { name: 'android.permission.health.READ_HEIGHT' },
        { name: 'android.permission.health.READ_BODY_FAT' },
        { name: 'android.permission.health.READ_BASAL_METABOLIC_RATE' }
    ];

    const profilePermissionsWrite = [
        { name: 'android.permission.health.WRITE_WEIGHT' },
        { name: 'android.permission.health.WRITE_HEIGHT' },
        { name: 'android.permission.health.WRITE_BODY_FAT' },
        { name: 'android.permission.health.WRITE_BASAL_METABOLIC_RATE' }
    ];

    //get all the preferences from config.xml, for every Health Connect permission
    const allVariables = configParser.getPlatformPreference('AllVariables', 'android');
    const fitnessVariables = configParser.getPlatformPreference('FitnessVariables', 'android');
    const healthVariables = configParser.getPlatformPreference('HealthVariables', 'android');
    const profileVariables = configParser.getPlatformPreference('ProfileVariables', 'android');

    // individual variables
    const heartRate = configParser.getPlatformPreference('HeartRate', 'android');
    const steps = configParser.getPlatformPreference('Steps', 'android');
    const weight = configParser.getPlatformPreference('Weight', 'android');
    const height = configParser.getPlatformPreference('Height', 'android');
    const calories = configParser.getPlatformPreference('CaloriesBurned', 'android');
    const sleep = configParser.getPlatformPreference('Sleep', 'android');
    const bloodPressure = configParser.getPlatformPreference('BloodPressure', 'android');
    const bloodGlucose = configParser.getPlatformPreference('BloodGlucose', 'android');
    const bodyFat = configParser.getPlatformPreference('BodyFatPercentage', 'android');
    const bmr = configParser.getPlatformPreference('BasalMetabolicRate', 'android');
    const speed = configParser.getPlatformPreference('WalkingSpeed', 'android');
    const distance = configParser.getPlatformPreference('Distance', 'android');

    // we'll use these to know if we should set individual permissions or not
    // e.g. when checking HeartRate, if all healthVariables were already set, we don't need to add it again
    var fitnessSet = false
    var healthSet = false
    var profileSet = false

    // we'll use these to know if we should write group permissions or not
    var heartRateSet = false
    var stepsSet = false
    var weightSet = false
    var heightSet = false
    var caloriesSet = false
    var sleepSet = false
    var bloodPressureSet = false
    var bloodGlucoseSet = false
    var bodyFatSet = false
    var bmrSet = false
    var speedSet = false
    var distanceSet = false



    // Android >= 14 dependencies, which should be included directly in the AndroidManifest.xml file

    console.log('About to read the AndroidManifest.xml file');

    // Read the AndroidManifest.xml file
    const manifestFilePath = path.join(projectRoot, 'platforms/android/app/src/main/AndroidManifest.xml');
    const manifestXmlString = fs.readFileSync(manifestFilePath, 'utf-8');

    console.log('About to parse the XML string');

    // Parse the XML string
    const parser = new DOMParser();
    const manifestXmlDoc = parser.parseFromString(manifestXmlString, 'text/xml');

    console.log('About to append permission');

    /*
    const newPermission = manifestXmlDoc.createElement('uses-permission');
    newPermission.setAttribute('android:name', 'android.permission.health.READ_HEART_RATE');
    manifestXmlDoc.documentElement.appendChild(newPermission);
    */

    // heartRate
    if (heartRate == "ReadWrite" || heartRate == "Read") {
        heartRateSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_HEART_RATE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (heartRate == "ReadWrite" || heartRate == "Write") {
        heartRateSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEART_RATE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // steps
    if (steps == "ReadWrite" || steps == "Read") {
        stepsSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_STEPS');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (steps == "ReadWrite" || steps == "Write") {
        stepsSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_STEPS');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // weight
    if (weight == "ReadWrite" || weight == "Read") {
        weightSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_WEIGHT');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (weight == "ReadWrite" || weight == "Write") {
        weightSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_WEIGHT');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // height
    if (height == "ReadWrite" || height == "Read") {
        heightSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_HEIGHT');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (height == "ReadWrite" || height == "Write") {
        heightSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEIGHT');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // calories
    if (calories == "ReadWrite" || calories == "Read") {
        caloriesSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_TOTAL_CALORIES_BURNED');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (calories == "ReadWrite" || calories == "Write") {
        caloriesSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // sleep
    if (sleep == "ReadWrite" || sleep == "Read") {
        sleepSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_SLEEP');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (sleep == "ReadWrite" || sleep == "Write") {
        sleepSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SLEEP');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // blood pressure
    if (bloodPressure == "ReadWrite" || bloodPressure == "Read") {
        bloodPressureSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_PRESSURE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (bloodPressure == "ReadWrite" || bloodPressure == "Write") {
        bloodPressureSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_PRESSURE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // blood glucose
    if (bloodGlucose == "ReadWrite" || bloodGlucose == "Read") {
        bloodGlucoseSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_GLUCOSE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (bloodGlucose == "ReadWrite" || bloodGlucose == "Write") {
        bloodGlucoseSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_GLUCOSE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // body fat
    if (bodyFat == "ReadWrite" || bodyFat == "Read") {
        bodyFatSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_BODY_FAT');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (bodyFat == "ReadWrite" || bodyFat == "Write") {
        bodyFatSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BODY_FAT');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // bmr
    if (bmr == "ReadWrite" || bmr == "Read") {
        bmrSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_BASAL_METABOLIC_RATE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (bmr == "ReadWrite" || bmr == "Write") {
        bmrSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BASAL_METABOLIC_RATE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // speed
    if (speed == "ReadWrite" || speed == "Read") {
        speedSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_SPEED');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (speed == "ReadWrite" || speed == "Write") {
        speedSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SPEED');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // distance
    if (distance == "ReadWrite" || distance == "Read") {
        distanceSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_DISTANCE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    if (distance == "ReadWrite" || distance == "Write") {
        distanceSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_DISTANCE');
        manifestXmlDoc.documentElement.appendChild(newPermission);
    }

    // process fitness variables
    if (fitnessVariables == "ReadWrite" || fitnessVariables == "Read") {

        fitnessSet = true

        if (!stepsSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_STEPS');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!caloriesSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_TOTAL_CALORIES_BURNED');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!speedSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_SPEED');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!distanceSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_DISTANCE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

    }

    if (fitnessVariables == "ReadWrite" || fitnessVariables == "Write") {

        fitnessSet = true

        if (!stepsSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_STEPS');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!caloriesSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!speedSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SPEED');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!distanceSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_DISTANCE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

    }

    // process health variables
    if (healthVariables == "ReadWrite" || healthVariables == "Read") {

        healthSet = true

        if (!heartRateSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_HEART_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!sleepSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_SLEEP');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!bloodPressureSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_PRESSURE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!bloodGlucoseSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_GLUCOSE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

    }

    if (healthVariables == "ReadWrite" || healthVariables == "Write") {

        healthSet = true

        if (!heartRateSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEART_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!sleepSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SLEEP');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!bloodPressureSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_PRESSURE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!bloodGlucoseSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_GLUCOSE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

    }

    // process profile variables
    if (profileVariables == "ReadWrite" || profileVariables == "Read") {

        profileSet = true

        if (!weightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_WEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!heightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_HEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!bodyFatSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BODY_FAT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!bmrSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BASAL_METABOLIC_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

    }

    if (profileVariables == "ReadWrite" || profileVariables == "Write") {

        profileSet = true

        if (!weightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_WEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!heightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!bodyFatSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BODY_FAT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!bmrSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BASAL_METABOLIC_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

    }


    // process AllVariables

    if (allVariables == "ReadWrite" || allVariables == "Read") {

        // fitness
        if (!fitnessSet && !stepsSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_STEPS');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!fitnessSet && !caloriesSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_TOTAL_CALORIES_BURNED');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!fitnessSet && !speedSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_SPEED');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!fitnessSet && !distanceSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_DISTANCE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        // health
        if (!healthSet && !heartRateSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_HEART_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!healthSet && !sleepSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_SLEEP');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!healthSet && !bloodPressureSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_PRESSURE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!healthSet && !bloodGlucoseSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_GLUCOSE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        // profile
        if (!profileSet && !weightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_WEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!profileSet && !heightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_HEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!profileSet && !bodyFatSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BODY_FAT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!profileSet && !bmrSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BASAL_METABOLIC_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }
        
    }

    if (allVariables == "ReadWrite" || allVariables == "Write") {

        // fitness
        if (!fitnessSet && !stepsSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_STEPS');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!fitnessSet && !caloriesSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!fitnessSet && !speedSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SPEED');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!fitnessSet && !distanceSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_DISTANCE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        // health
        if (!healthSet && !heartRateSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEART_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!healthSet && !sleepSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SLEEP');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!healthSet && !bloodPressureSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_PRESSURE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!healthSet && !bloodGlucoseSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_GLUCOSE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        // profile
        if (!profileSet && !weightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_WEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!profileSet && !heightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!profileSet && !bodyFatSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BODY_FAT');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }

        if (!profileSet && !bmrSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BASAL_METABOLIC_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);
        }
        
    }


    // if there is no AllVariables nor anything else, then by default we add all the permissions
    if (allVariables == null && fitnessVariables == null && healthVariables == null && profileVariables == null
        && heartRate == null && steps == null && weight == null && height == null
        && calories == null && sleep == null && bloodPressure == null && bloodGlucose == null
        && bodyFat == null && bmr == null && speed == null && distance == null) {

            fitnessPermissionsRead.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);
            });
            
            fitnessPermissionsWrite.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);
            });
    
            healthPermissionsRead.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);
            });
            
            healthPermissionsWrite.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);
            });
    
            profilePermissionsRead.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);
            });
            
            profilePermissionsWrite.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);
            });

        }

    console.log('About to serialize');

    // Serialize the updated XML document back to string
    const serializer = new XMLSerializer();
    const updatedManifestXmlString = serializer.serializeToString(manifestXmlDoc);

    console.log('About to write the file again');

    // Write the updated XML string back to the same file
    fs.writeFileSync(manifestFilePath, updatedManifestXmlString, 'utf-8');
    

}
