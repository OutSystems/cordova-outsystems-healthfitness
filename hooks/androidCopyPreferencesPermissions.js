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

    // add background job permissions to AndroidManfiest.xml
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

        // Android >= 14
        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_HEART_RATE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        // Android <= 13
        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_HEART_RATE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (heartRate == "ReadWrite" || heartRate == "Write") {
        heartRateSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEART_RATE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_HEART_RATE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // steps
    if (steps == "ReadWrite" || steps == "Read") {
        stepsSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_STEPS');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_STEPS');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (steps == "ReadWrite" || steps == "Write") {
        stepsSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_STEPS');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_STEPS');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // weight
    if (weight == "ReadWrite" || weight == "Read") {
        weightSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_WEIGHT');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_WEIGHT');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (weight == "ReadWrite" || weight == "Write") {
        weightSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_WEIGHT');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_WEIGHT');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // height
    if (height == "ReadWrite" || height == "Read") {
        heightSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_HEIGHT');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_HEIGHT');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (height == "ReadWrite" || height == "Write") {
        heightSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEIGHT');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_HEIGHT');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // calories
    if (calories == "ReadWrite" || calories == "Read") {
        caloriesSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_TOTAL_CALORIES_BURNED');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_TOTAL_CALORIES_BURNED');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (calories == "ReadWrite" || calories == "Write") {
        caloriesSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_TOTAL_CALORIES_BURNED');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // sleep
    if (sleep == "ReadWrite" || sleep == "Read") {
        sleepSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_SLEEP');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_SLEEP');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (sleep == "ReadWrite" || sleep == "Write") {
        sleepSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SLEEP');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_SLEEP');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // blood pressure
    if (bloodPressure == "ReadWrite" || bloodPressure == "Read") {
        bloodPressureSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_PRESSURE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BLOOD_PRESSURE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (bloodPressure == "ReadWrite" || bloodPressure == "Write") {
        bloodPressureSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_PRESSURE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BLOOD_PRESSURE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // blood glucose
    if (bloodGlucose == "ReadWrite" || bloodGlucose == "Read") {
        bloodGlucoseSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_GLUCOSE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BLOOD_GLUCOSE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (bloodGlucose == "ReadWrite" || bloodGlucose == "Write") {
        bloodGlucoseSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_GLUCOSE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BLOOD_GLUCOSE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // body fat
    if (bodyFat == "ReadWrite" || bodyFat == "Read") {
        bodyFatSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_BODY_FAT');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BODY_FAT');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (bodyFat == "ReadWrite" || bodyFat == "Write") {
        bodyFatSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BODY_FAT');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BODY_FAT');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // bmr
    if (bmr == "ReadWrite" || bmr == "Read") {
        bmrSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_BASAL_METABOLIC_RATE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BASAL_METABOLIC_RATE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (bmr == "ReadWrite" || bmr == "Write") {
        bmrSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BASAL_METABOLIC_RATE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BASAL_METABOLIC_RATE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // speed
    if (speed == "ReadWrite" || speed == "Read") {
        speedSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_SPEED');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_SPEED');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (speed == "ReadWrite" || speed == "Write") {
        speedSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SPEED');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_SPEED');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // distance
    if (distance == "ReadWrite" || distance == "Read") {
        distanceSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.READ_DISTANCE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_DISTANCE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    if (distance == "ReadWrite" || distance == "Write") {
        distanceSet = true

        const newPermission = manifestXmlDoc.createElement('uses-permission');
        newPermission.setAttribute('android:name', 'android.permission.health.WRITE_DISTANCE');
        manifestXmlDoc.documentElement.appendChild(newPermission);

        const newItem = permissionsXmlDoc.createElement('item');
        const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_DISTANCE');
        newItem.appendChild(textNode);
        arrayElement.appendChild(newItem);
    }

    // process fitness variables
    if (fitnessVariables == "ReadWrite" || fitnessVariables == "Read") {

        fitnessSet = true

        if (!stepsSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_STEPS');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_STEPS');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!caloriesSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_TOTAL_CALORIES_BURNED');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_TOTAL_CALORIES_BURNED');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!speedSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_SPEED');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_SPEED');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!distanceSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_DISTANCE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_DISTANCE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

    }

    if (fitnessVariables == "ReadWrite" || fitnessVariables == "Write") {

        fitnessSet = true

        if (!stepsSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_STEPS');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_STEPS');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!caloriesSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_TOTAL_CALORIES_BURNED');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!speedSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SPEED');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_SPEED');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!distanceSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_DISTANCE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_DISTANCE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

    }

    // process health variables
    if (healthVariables == "ReadWrite" || healthVariables == "Read") {

        healthSet = true

        if (!heartRateSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_HEART_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_HEART_RATE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!sleepSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_SLEEP');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_SLEEP');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!bloodPressureSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_PRESSURE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BLOOD_PRESSURE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!bloodGlucoseSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_GLUCOSE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BLOOD_GLUCOSE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

    }

    if (healthVariables == "ReadWrite" || healthVariables == "Write") {

        healthSet = true

        if (!heartRateSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEART_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_HEART_RATE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!sleepSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SLEEP');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_SLEEP');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!bloodPressureSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_PRESSURE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BLOOD_PRESSURE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!bloodGlucoseSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_GLUCOSE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BLOOD_GLUCOSE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

    }

    // process profile variables
    if (profileVariables == "ReadWrite" || profileVariables == "Read") {

        profileSet = true

        if (!weightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_WEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_WEIGHT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!heightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_HEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_HEIGHT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!bodyFatSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BODY_FAT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BODY_FAT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!bmrSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BASAL_METABOLIC_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BASAL_METABOLIC_RATE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

    }

    if (profileVariables == "ReadWrite" || profileVariables == "Write") {

        profileSet = true

        if (!weightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_WEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_WEIGHT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!heightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_HEIGHT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!bodyFatSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BODY_FAT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BODY_FAT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!bmrSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BASAL_METABOLIC_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BASAL_METABOLIC_RATE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

    }


    // process AllVariables

    if (allVariables == "ReadWrite" || allVariables == "Read") {

        // fitness
        if (!fitnessSet && !stepsSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_STEPS');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_STEPS');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!fitnessSet && !caloriesSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_TOTAL_CALORIES_BURNED');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_TOTAL_CALORIES_BURNED');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!fitnessSet && !speedSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_SPEED');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_SPEED');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!fitnessSet && !distanceSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_DISTANCE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_DISTANCE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        // health
        if (!healthSet && !heartRateSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_HEART_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_HEART_RATE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!healthSet && !sleepSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_SLEEP');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_SLEEP');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!healthSet && !bloodPressureSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_PRESSURE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BLOOD_PRESSURE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!healthSet && !bloodGlucoseSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BLOOD_GLUCOSE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BLOOD_GLUCOSE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        // profile
        if (!profileSet && !weightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_WEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_WEIGHT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!profileSet && !heightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_HEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_HEIGHT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!profileSet && !bodyFatSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BODY_FAT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BODY_FAT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!profileSet && !bmrSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.READ_BASAL_METABOLIC_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.READ_BASAL_METABOLIC_RATE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }
        
    }

    if (allVariables == "ReadWrite" || allVariables == "Write") {

        // fitness
        if (!fitnessSet && !stepsSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_STEPS');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_STEPS');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!fitnessSet && !caloriesSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_TOTAL_CALORIES_BURNED');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_TOTAL_CALORIES_BURNED');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!fitnessSet && !speedSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SPEED');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_SPEED');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!fitnessSet && !distanceSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_DISTANCE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_DISTANCE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        // health
        if (!healthSet && !heartRateSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEART_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_HEART_RATE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!healthSet && !sleepSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_SLEEP');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_SLEEP');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!healthSet && !bloodPressureSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_PRESSURE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BLOOD_PRESSURE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!healthSet && !bloodGlucoseSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BLOOD_GLUCOSE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BLOOD_GLUCOSE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        // profile
        if (!profileSet && !weightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_WEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_WEIGHT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!profileSet && !heightSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_HEIGHT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_HEIGHT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!profileSet && !bodyFatSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BODY_FAT');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BODY_FAT');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }

        if (!profileSet && !bmrSet) {
            const newPermission = manifestXmlDoc.createElement('uses-permission');
            newPermission.setAttribute('android:name', 'android.permission.health.WRITE_BASAL_METABOLIC_RATE');
            manifestXmlDoc.documentElement.appendChild(newPermission);

            const newItem = permissionsXmlDoc.createElement('item');
            const textNode = permissionsXmlDoc.createTextNode('android.permission.health.WRITE_BASAL_METABOLIC_RATE');
            newItem.appendChild(textNode);
            arrayElement.appendChild(newItem);
        }
        
    }

    // if there is no AllVariables nor anything else, then by default we add all the permissions
    if (allVariables == "" && fitnessVariables == "" && healthVariables == "" && profileVariables == ""
        && heartRate == "" && steps == "" && weight == "" && height == ""
        && calories == "" && sleep == "" && bloodPressure == "" && bloodGlucose == ""
        && bodyFat == "" && bmr == "" && speed == "" && distance == "") {

            fitnessPermissionsRead.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);

                const newItem = permissionsXmlDoc.createElement('item');
                const textNode = permissionsXmlDoc.createTextNode(permission.name);
                newItem.appendChild(textNode);
                arrayElement.appendChild(newItem);
            });
            
            fitnessPermissionsWrite.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);

                const newItem = permissionsXmlDoc.createElement('item');
                const textNode = permissionsXmlDoc.createTextNode(permission.name);
                newItem.appendChild(textNode);
                arrayElement.appendChild(newItem);
            });
    
            healthPermissionsRead.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);

                const newItem = permissionsXmlDoc.createElement('item');
                const textNode = permissionsXmlDoc.createTextNode(permission.name);
                newItem.appendChild(textNode);
                arrayElement.appendChild(newItem);
            });
            
            healthPermissionsWrite.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);

                const newItem = permissionsXmlDoc.createElement('item');
                const textNode = permissionsXmlDoc.createTextNode(permission.name);
                newItem.appendChild(textNode);
                arrayElement.appendChild(newItem);
            });
    
            profilePermissionsRead.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);

                const newItem = permissionsXmlDoc.createElement('item');
                const textNode = permissionsXmlDoc.createTextNode(permission.name);
                newItem.appendChild(textNode);
                arrayElement.appendChild(newItem);
            });
            
            profilePermissionsWrite.forEach(permission => {
                const newPermission = manifestXmlDoc.createElement('uses-permission');
                newPermission.setAttribute('android:name', permission.name);
                manifestXmlDoc.documentElement.appendChild(newPermission);

                const newItem = permissionsXmlDoc.createElement('item');
                const textNode = permissionsXmlDoc.createTextNode(permission.name);
                newItem.appendChild(textNode);
                arrayElement.appendChild(newItem);
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

        const notificationsPermission = manifestXmlDoc.createElement('uses-permission');
        notificationsPermission.setAttribute('android:name', 'android.permission.POST_NOTIFICATIONS');
        manifestXmlDoc.documentElement.appendChild(notificationsPermission);

        const activityPermission = manifestXmlDoc.createElement('uses-permission');
        activityPermission.setAttribute('android:name', 'android.permission.ACTIVITY_RECOGNITION');
        manifestXmlDoc.documentElement.appendChild(activityPermission);

        const foregroundServicePermission = manifestXmlDoc.createElement('uses-permission');
        foregroundServicePermission.setAttribute('android:name', 'android.permission.FOREGROUND_SERVICE');
        manifestXmlDoc.documentElement.appendChild(foregroundServicePermission);

        const foregroundServiceHealthPermission = manifestXmlDoc.createElement('uses-permission');
        foregroundServiceHealthPermission.setAttribute('android:name', 'android.permission.FOREGROUND_SERVICE_HEALTH');
        manifestXmlDoc.documentElement.appendChild(foregroundServiceHealthPermission);

        const highSamplingPermission = manifestXmlDoc.createElement('uses-permission');
        highSamplingPermission.setAttribute('android:name', 'android.permission.HIGH_SAMPLING_RATE_SENSORS');
        manifestXmlDoc.documentElement.appendChild(highSamplingPermission);

        // serialize the updated XML document back to string
        const serializer = new XMLSerializer();
        const updatedManifestXmlString = serializer.serializeToString(manifestXmlDoc);

        // write the updated XML string back to the same file
        fs.writeFileSync(manifestFilePath, updatedManifestXmlString, 'utf-8');
    }

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
