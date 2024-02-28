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

    const parser = new DOMParser();
        
    // add health connect permissions to AndroidManifest.xml and health_permissions.xml files
    addHealthConnectPermissionsToXmlFiles(configParser, projectRoot, parser);

    // add background job permissions to AndroidManifest.xml
    addBackgroundJobPermissionsToManifest(configParser, projectRoot, parser);

    // copy notification title and content for notificaiton for Foreground Service
    copyNotificationContent(configParser, projectRoot, parser);
};

function addHealthConnectPermissionsToXmlFiles(configParser, projectRoot, parser) {

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

    // Android >= 14 dependencies should be included directly in the AndroidManifest.xml file
    // Read the AndroidManifest.xml file
    const manifestFilePath = path.join(projectRoot, 'platforms/android/app/src/main/AndroidManifest.xml');
    const manifestXmlString = fs.readFileSync(manifestFilePath, 'utf-8');

    // Parse the XML string
    const manifestXmlDoc = parser.parseFromString(manifestXmlString, 'text/xml');

    // Android <= 13 dependencies should be included in a separate XML file
    // Create the health_permissions.xml file
    const permissionsXmlDoc = parser.parseFromString('<?xml version="1.0" encoding="utf-8"?><resources><array name="health_permissions"></array></resources>', 'text/xml');
    // Get the <array> element
    const arrayElement = permissionsXmlDoc.getElementsByTagName('array')[0];
 

    // heartRate
    if (heartRate == "ReadWrite" || heartRate == "Read") {
        heartRateSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_HEART_RATE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_HEART_RATE')
    }

    if (heartRate == "ReadWrite" || heartRate == "Write") {
        heartRateSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_HEART_RATE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_HEART_RATE')
    }

    // steps
    if (steps == "ReadWrite" || steps == "Read") {
        stepsSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_STEPS')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_STEPS')
    }

    if (steps == "ReadWrite" || steps == "Write") {
        stepsSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_STEPS')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_STEPS')
    }

    // weight
    if (weight == "ReadWrite" || weight == "Read") {
        weightSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_WEIGHT')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_WEIGHT')
    }

    if (weight == "ReadWrite" || weight == "Write") {
        weightSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_WEIGHT')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_WEIGHT')
    }

    // height
    if (height == "ReadWrite" || height == "Read") {
        heightSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_HEIGHT')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_HEIGHT')
    }

    if (height == "ReadWrite" || height == "Write") {
        heightSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_HEIGHT')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_HEIGHT')
    }

    // calories
    if (calories == "ReadWrite" || calories == "Read") {
        caloriesSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_TOTAL_CALORIES_BURNED')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_TOTAL_CALORIES_BURNED')
    }

    if (calories == "ReadWrite" || calories == "Write") {
        caloriesSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED')
    }

    // sleep
    if (sleep == "ReadWrite" || sleep == "Read") {
        sleepSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_SLEEP')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_SLEEP')
    }

    if (sleep == "ReadWrite" || sleep == "Write") {
        sleepSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_SLEEP')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_SLEEP')
    }

    // blood pressure
    if (bloodPressure == "ReadWrite" || bloodPressure == "Read") {
        bloodPressureSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BLOOD_PRESSURE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BLOOD_PRESSURE')
    }

    if (bloodPressure == "ReadWrite" || bloodPressure == "Write") {
        bloodPressureSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BLOOD_PRESSURE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BLOOD_PRESSURE')
    }

    // blood glucose
    if (bloodGlucose == "ReadWrite" || bloodGlucose == "Read") {
        bloodGlucoseSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BLOOD_GLUCOSE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BLOOD_GLUCOSE')
    }

    if (bloodGlucose == "ReadWrite" || bloodGlucose == "Write") {
        bloodGlucoseSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BLOOD_GLUCOSE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BLOOD_GLUCOSE')
    }

    // body fat
    if (bodyFat == "ReadWrite" || bodyFat == "Read") {
        bodyFatSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BODY_FAT')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BODY_FAT')
    }

    if (bodyFat == "ReadWrite" || bodyFat == "Write") {
        bodyFatSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BODY_FAT')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BODY_FAT')
    }

    // bmr
    if (bmr == "ReadWrite" || bmr == "Read") {
        bmrSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BASAL_METABOLIC_RATE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BASAL_METABOLIC_RATE')
    }

    if (bmr == "ReadWrite" || bmr == "Write") {
        bmrSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BASAL_METABOLIC_RATE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BASAL_METABOLIC_RATE')
    }

    // speed
    if (speed == "ReadWrite" || speed == "Read") {
        speedSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_SPEED')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_SPEED')
    }

    if (speed == "ReadWrite" || speed == "Write") {
        speedSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_SPEED')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_SPEED')
    }

    // distance
    if (distance == "ReadWrite" || distance == "Read") {
        distanceSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_DISTANCE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_DISTANCE')
    }

    if (distance == "ReadWrite" || distance == "Write") {
        distanceSet = true
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_DISTANCE')
        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_DISTANCE')
    }

    // process fitness variables
    if (fitnessVariables == "ReadWrite" || fitnessVariables == "Read") {

        fitnessSet = true

        if (!stepsSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_STEPS')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_STEPS')
        }

        if (!caloriesSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_TOTAL_CALORIES_BURNED')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_TOTAL_CALORIES_BURNED')
        }

        if (!speedSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_SPEED')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_SPEED')
        }

        if (!distanceSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_DISTANCE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_DISTANCE')
        }

    }

    if (fitnessVariables == "ReadWrite" || fitnessVariables == "Write") {

        fitnessSet = true

        if (!stepsSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_STEPS')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_STEPS')
        }

        if (!caloriesSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED')
        }

        if (!speedSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_SPEED')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_SPEED')
        }

        if (!distanceSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_DISTANCE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_DISTANCE')
        }

    }

    // process health variables
    if (healthVariables == "ReadWrite" || healthVariables == "Read") {

        healthSet = true

        if (!heartRateSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_HEART_RATE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_HEART_RATE')
        }

        if (!sleepSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_SLEEP')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_SLEEP')
        }

        if (!bloodPressureSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BLOOD_PRESSURE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BLOOD_PRESSURE')
        }

        if (!bloodGlucoseSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BLOOD_GLUCOSE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BLOOD_GLUCOSE')
        }

    }

    if (healthVariables == "ReadWrite" || healthVariables == "Write") {

        healthSet = true

        if (!heartRateSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_HEART_RATE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_HEART_RATE')
        }

        if (!sleepSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_SLEEP')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_SLEEP')
        }

        if (!bloodPressureSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BLOOD_PRESSURE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BLOOD_PRESSURE')
        }

        if (!bloodGlucoseSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BLOOD_GLUCOSE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BLOOD_GLUCOSE')
        }

    }

    // process profile variables
    if (profileVariables == "ReadWrite" || profileVariables == "Read") {

        profileSet = true

        if (!weightSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_WEIGHT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_WEIGHT')
        }

        if (!heightSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_HEIGHT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_HEIGHT')
        }

        if (!bodyFatSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BODY_FAT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BODY_FAT')
        }

        if (!bmrSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BASAL_METABOLIC_RATE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BASAL_METABOLIC_RATE')
        }

    }

    if (profileVariables == "ReadWrite" || profileVariables == "Write") {

        profileSet = true

        if (!weightSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_WEIGHT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_WEIGHT')
        }

        if (!heightSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_HEIGHT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_HEIGHT')
        }

        if (!bodyFatSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BODY_FAT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BODY_FAT')
        }

        if (!bmrSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BASAL_METABOLIC_RATE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BASAL_METABOLIC_RATE')
        }

    }


    // process AllVariables

    if (allVariables == "ReadWrite" || allVariables == "Read") {

        // fitness
        if (!fitnessSet && !stepsSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_STEPS')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_STEPS')
        }

        if (!fitnessSet && !caloriesSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_TOTAL_CALORIES_BURNED')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_TOTAL_CALORIES_BURNED')
        }

        if (!fitnessSet && !speedSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_SPEED')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_SPEED')
        }

        if (!fitnessSet && !distanceSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_DISTANCE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_DISTANCE')
        }

        // health
        if (!healthSet && !heartRateSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_HEART_RATE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_HEART_RATE')
        }

        if (!healthSet && !sleepSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_SLEEP')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_SLEEP')
        }

        if (!healthSet && !bloodPressureSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BLOOD_PRESSURE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BLOOD_PRESSURE')
        }

        if (!healthSet && !bloodGlucoseSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BLOOD_GLUCOSE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BLOOD_GLUCOSE')
        }

        // profile
        if (!profileSet && !weightSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_WEIGHT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_WEIGHT')
        }

        if (!profileSet && !heightSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_HEIGHT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_HEIGHT')
        }

        if (!profileSet && !bodyFatSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BODY_FAT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BODY_FAT')
        }

        if (!profileSet && !bmrSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_BASAL_METABOLIC_RATE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_BASAL_METABOLIC_RATE')
        }
        
    }

    if (allVariables == "ReadWrite" || allVariables == "Write") {

        // fitness
        if (!fitnessSet && !stepsSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_STEPS')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_STEPS')
        }

        if (!fitnessSet && !caloriesSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED')
        }

        if (!fitnessSet && !speedSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_SPEED')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_SPEED')
        }

        if (!fitnessSet && !distanceSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_DISTANCE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_DISTANCE')
        }

        // health
        if (!healthSet && !heartRateSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_HEART_RATE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_HEART_RATE')
        }

        if (!healthSet && !sleepSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_SLEEP')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_SLEEP')
        }

        if (!healthSet && !bloodPressureSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BLOOD_PRESSURE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BLOOD_PRESSURE')
        }

        if (!healthSet && !bloodGlucoseSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BLOOD_GLUCOSE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BLOOD_GLUCOSE')
        }

        // profile
        if (!profileSet && !weightSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_WEIGHT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_WEIGHT')
        }

        if (!profileSet && !heightSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_HEIGHT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_HEIGHT')
        }

        if (!profileSet && !bodyFatSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BODY_FAT')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BODY_FAT')
        }

        if (!profileSet && !bmrSet) {
            addEntryToManifest(manifestXmlDoc, 'android.permission.health.WRITE_BASAL_METABOLIC_RATE')
            addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.WRITE_BASAL_METABOLIC_RATE')
        }
        
    }

    // if there is no AllVariables nor anything else, then by default we add all the permissions
    if (allVariables == "" && fitnessVariables == "" && healthVariables == "" && profileVariables == ""
        && heartRate == "" && steps == "" && weight == "" && height == ""
        && calories == "" && sleep == "" && bloodPressure == "" && bloodGlucose == ""
        && bodyFat == "" && bmr == "" && speed == "" && distance == "") {

            fitnessPermissionsRead.forEach(permission => {
                addEntryToManifest(manifestXmlDoc, permission.name)
                addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, permission.name)
            });
            
            fitnessPermissionsWrite.forEach(permission => {
                addEntryToManifest(manifestXmlDoc, permission.name)
                addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, permission.name)
            });
    
            healthPermissionsRead.forEach(permission => {
                addEntryToManifest(manifestXmlDoc, permission.name)
                addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, permission.name)
            });
            
            healthPermissionsWrite.forEach(permission => {
                addEntryToManifest(manifestXmlDoc, permission.name)
                addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, permission.name)
            });
    
            profilePermissionsRead.forEach(permission => {
                addEntryToManifest(manifestXmlDoc, permission.name)
                addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, permission.name)
            });
            
            profilePermissionsWrite.forEach(permission => {
                addEntryToManifest(manifestXmlDoc, permission.name)
                addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, permission.name)
            });

        }

    // Serialize the updated XML document back to string
    const serializer = new XMLSerializer();

    // Android >= 14
    const updatedManifestXmlString = serializer.serializeToString(manifestXmlDoc);
    // Write the updated XML string back to the same file
    fs.writeFileSync(manifestFilePath, updatedManifestXmlString, 'utf-8');

    // Android <= 13
    const updatedPermissionsXmlString = serializer.serializeToString(permissionsXmlDoc);
    const permissionsXmlFilePath = path.join(projectRoot, 'platforms/android/app/src/main/res/values/health_permissions.xml');
    // Write the updated XML string back to the same file
    fs.writeFileSync(permissionsXmlFilePath, updatedPermissionsXmlString, 'utf-8');

}

function addBackgroundJobPermissionsToManifest(configParser, projectRoot, parser) {

    const disableBackgroundJobs = configParser.getPlatformPreference('DisableBackgroundJobs', 'android');

    // we want to include the permissions by default
    // if disableBackgroundJobs == true then we don't want to include the permissions in the manfiest
    if (disableBackgroundJobs !== "true") {

        const manifestFilePath = path.join(projectRoot, 'platforms/android/app/src/main/AndroidManifest.xml');
        const manifestXmlString = fs.readFileSync(manifestFilePath, 'utf-8');

        // Parse the XML string
        const manifestXmlDoc = parser.parseFromString(manifestXmlString, 'text/xml');

        // add permissions to XML document
        addEntryToManifest(manifestXmlDoc, 'android.permission.POST_NOTIFICATIONS')
        addEntryToManifest(manifestXmlDoc, 'android.permission.ACTIVITY_RECOGNITION')
        addEntryToManifest(manifestXmlDoc, 'android.permission.FOREGROUND_SERVICE')
        addEntryToManifest(manifestXmlDoc, 'android.permission.FOREGROUND_SERVICE_HEALTH')
        addEntryToManifest(manifestXmlDoc, 'android.permission.HIGH_SAMPLING_RATE_SENSORS')

        // serialize the updated XML document back to string
        const serializer = new XMLSerializer();
        const updatedManifestXmlString = serializer.serializeToString(manifestXmlDoc);

        // write the updated XML string back to the same file
        fs.writeFileSync(manifestFilePath, updatedManifestXmlString, 'utf-8');
    }

}

function addEntryToManifest(manifestXmlDoc, permission) {
    const newPermission = manifestXmlDoc.createElement('uses-permission');
    newPermission.setAttribute('android:name', permission);
    manifestXmlDoc.documentElement.appendChild(newPermission);
}

function addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, permission) {
    const newItem = permissionsXmlDoc.createElement('item');
    const textNode = permissionsXmlDoc.createTextNode(permission);
    newItem.appendChild(textNode);
    arrayElement.appendChild(newItem);
}

function copyNotificationContent(configParser, projectRoot, parser) {

    // get values from config.xml
    var notificationTitle = configParser.getPlatformPreference('BackgroundNotificationTitle', 'android');
    var notificationDescription = configParser.getPlatformPreference('BackgroundNotificationDescription', 'android');

    if (notificationTitle == "") {
        notificationTitle = "Measuring your health and fitness data."
    }

    if (notificationDescription == "") {
        notificationDescription = "The app is running in the background."
    }

    // insert values in strings.xml
    const stringsXmlPath = path.join(projectRoot, 'platforms/android/app/src/main/res/values/strings.xml');
    const stringsXmlString = fs.readFileSync(stringsXmlPath, 'utf-8');
    const stringsXmlDoc = parser.parseFromString(stringsXmlString, 'text/xml')
    const stringElements = stringsXmlDoc.getElementsByTagName('string');

    // set text for each <string> element
    for (let i = 0; i < stringElements.length; i++) {
        const name = stringElements[i].getAttribute('name');
        if (name == "background_notification_title") {
            stringElements[i].textContent = notificationTitle;
        }
        else if (name == "background_notification_description") {
            stringElements[i].textContent = notificationDescription;
        }
    }

    // serialize the updated XML document back to string
    const serializer = new XMLSerializer();
    const updatedXmlString = serializer.serializeToString(stringsXmlDoc);

    // write the updated XML string back to the same file
    fs.writeFileSync(stringsXmlPath, updatedXmlString, 'utf-8');
}
