const fs = require('fs');
const path = require('path');
const { ConfigParser } = require('cordova-common');
const { DOMParser, XMLSerializer } = require('xmldom');

const READ = "Read"
const WRITE = "Write"
const READWRITE = "ReadWrite"

let permissions = {
    HeartRate: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        // we'll use these to know if we should write group permissions or not
        wasSet: false
    },
    Steps: {
        variableName: "Steps",
        readPermission: "android.permission.health.READ_STEPS",
        writePermission: "android.permission.health.WRITE_STEPS",
        configValue: undefined,
        wasSet: false
    },
    Weight: {
        variableName: "Weight",
        readPermission: "android.permission.health.READ_WEIGHT",
        writePermission: "android.permission.health.WRITE_WEIGHT",
        configValue: undefined,
        wasSet: false
    },
    Height: {
        variableName: "Height",
        readPermission: "android.permission.health.READ_HEIGHT",
        writePermission: "android.permission.health.WRITE_HEIGHT",
        configValue: undefined,
        wasSet: false
    },
    CaloriesBurned: {
        variableName: "CaloriesBurned",
        readPermission: "android.permission.health.READ_TOTAL_CALORIES_BURNED",
        writePermission: "android.permission.health.WRITE_TOTAL_CALORIES_BURNED",
        configValue: undefined,
        wasSet: false
    },
    Sleep: {
        variableName: "Sleep",
        readPermission: "android.permission.health.READ_SLEEP",
        writePermission: "android.permission.health.WRITE_SLEEP",
        configValue: undefined,
        wasSet: false
    },
    BloodPressure: {
        variableName: "BloodPressure",
        readPermission: "android.permission.health.READ_BLOOD_PRESSURE",
        writePermission: "android.permission.health.WRITE_BLOOD_PRESSURE",
        configValue: undefined,
        wasSet: false
    },
    BloodGlucose: {
        variableName: "BloodGlucose",
        readPermission: "android.permission.health.READ_BLOOD_GLUCOSE",
        writePermission: "android.permission.health.WRITE_BLOOD_GLUCOSE",
        configValue: undefined,
        wasSet: false
    },
    BodyFatPercentage: {
        variableName: "BodyFatPercentage",
        readPermission: "android.permission.health.READ_BODY_FAT",
        writePermission: "android.permission.health.WRITE_BODY_FAT",
        configValue: undefined,
        wasSet: false
    },
    BasalMetabolicRate: {
        variableName: "BasalMetabolicRate",
        readPermission: "android.permission.health.READ_BASAL_METABOLIC_RATE",
        writePermission: "android.permission.health.WRITE_BASAL_METABOLIC_RATE",
        configValue: undefined,
        wasSet: false
    },
    WalkingSpeed: {
        variableName: "WalkingSpeed",
        readPermission: "android.permission.health.READ_SPEED",
        writePermission: "android.permission.health.WRITE_SPEED",
        configValue: undefined,
        wasSet: false
    },
    Distance: {
        variableName: "Distance",
        readPermission: "android.permission.health.READ_DISTANCE",
        writePermission: "android.permission.health.WRITE_DISTANCE",
        configValue: undefined,
        wasSet: false
    },
    OxygenSaturation: {
        variableName: "OxygenSaturation",
        readPermission: "android.permission.health.READ_OXYGEN_SATURATION",
        writePermission: "android.permission.health.WRITE_OXYGEN_SATURATION",
        configValue: undefined,
        wasSet: false
    }
}

let groupPermissions = {
    AllVariables: {
        variableName: "AllVariables",
        configValue: undefined,
        wasSet: false,
        groupVariables: []
    },
    FitnessVariables: {
        variableName: "FitnessVariables",
        configValue: undefined,
        // we'll use these to know if we should set individual permissions or not
        // e.g. when checking HeartRate, if all healthVariables were already set, we don't need to add it again
        wasSet: false,
        groupVariables: ["Steps", "CaloriesBurned", "WalkingSpeed", "Distance"]
    },
    HealthVariables: {
        variableName: "HealthVariables",
        configValue: undefined,
        wasSet: false,
        groupVariables: ["HeartRate", "Sleep", "BloodPressure", "BloodGlucose", "OxygenSaturation"]
    },
    ProfileVariables: {
        variableName: "ProfileVariables",
        configValue: undefined,
        wasSet: false,
        groupVariables: ["Weight", "Height", "BodyFatPercentage", "BasalMetabolicRate"]
    }
}

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

    for(const key in permissions){
        permissions[key].configValue = configParser.getPlatformPreference(permissions[key].variableName, 'android');
    }

    for(const key in groupPermissions){
        groupPermissions[key].configValue = configParser.getPlatformPreference(groupPermissions[key].variableName, 'android');
    }

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
 
    // process each individual variable
    for(const key in permissions){
        let p = permissions[key]
        if (p.configValue == READWRITE || p.configValue == READ) {
            p.wasSet = true;
            processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, p.readPermission)
        }
        if (p.configValue == READWRITE || p.configValue == WRITE) {
            p.wasSet = true;
            processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, p.writePermission)
        }
    }

    // process group variables
    for(const key in groupPermissions){
        let p = groupPermissions[key]
        if (p.configValue == READWRITE || p.configValue == READ) {
            p.wasSet = true;
            p.groupVariables.forEach( v => {
                if (!permissions[v].wasSet) {
                    processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, permissions[v].readPermission)
                }
            })
        }
        if (p.configValue == READWRITE || p.configValue == WRITE) {
            p.wasSet = true;
            p.groupVariables.forEach( v => {
                if (!permissions[v].wasSet) {
                    processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, permissions[v].writePermission)
                }
            })
        }
    }

    let permissionValues = Object.values(permissions)
    let groupPermissionValues = Object.values(groupPermissions)

    // process AllVariables
    if (groupPermissions.AllVariables.configValue == READWRITE || groupPermissions.AllVariables.configValue == READ) {   
        processAllVariables(manifestXmlDoc, permissionsXmlDoc, arrayElement, READ, groupPermissionValues)

    }

    if ((groupPermissions.AllVariables.configValue == READWRITE || groupPermissions.AllVariables.configValue == WRITE)) {  
        processAllVariables(manifestXmlDoc, permissionsXmlDoc, arrayElement, WRITE, groupPermissionValues)
    }
    
    let numberOfPermissions = permissionValues.filter(p => p.configValue != "").length + groupPermissionValues.filter(p => p.configValue != "").length

    // if there is no AllVariables nor anything else, then by default we add all the permissions
    if (numberOfPermissions == 0) {
        permissionValues.forEach( p => {
            processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, p.readPermission)
            processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, p.writePermission)
        })
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

function processAllVariables(manifestXmlDoc, permissionsXmlDoc, arrayElement, permissionOperation, groupPermissionsValues) {
    groupPermissionsValues.forEach(p => {
        p.groupVariables.forEach( v => {
            if (!p.wasSet && !permissions[v].wasSet) {
                processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, permissionOperation == READ ? permissions[v].readPermission : permissions[v].writePermission)
            }
        })
    })  
}

function processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, permissionOperation) {
    addEntryToManifest(manifestXmlDoc, permissionOperation)
    addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, permissionOperation)
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
        addEntryToManifest(manifestXmlDoc, 'com.google.android.gms.permission.ACTIVITY_RECOGNITION') // necessary for API 28 and below
        addEntryToManifest(manifestXmlDoc, 'android.permission.FOREGROUND_SERVICE')
        addEntryToManifest(manifestXmlDoc, 'android.permission.FOREGROUND_SERVICE_HEALTH')
        addEntryToManifest(manifestXmlDoc, 'android.permission.HIGH_SAMPLING_RATE_SENSORS')
        addEntryToManifest(manifestXmlDoc, 'android.permission.SCHEDULE_EXACT_ALARM')

        // add permissions necessary on Android 15 (API 35)
        addEntryToManifest(manifestXmlDoc, 'android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND')
        
        const permissionsXmlDoc = parser.parseFromString('<?xml version="1.0" encoding="utf-8"?><resources><array name="health_permissions"></array></resources>', 'text/xml');
        const arrayElement = permissionsXmlDoc.getElementsByTagName('array')[0];

        addEntryToPermissionsXML(permissionsXmlDoc, arrayElement, 'android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND')

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
