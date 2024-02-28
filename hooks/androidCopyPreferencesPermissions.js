const fs = require('fs');
const path = require('path');
const { ConfigParser } = require('cordova-common');
const et = require('elementtree');
const { DOMParser, XMLSerializer } = require('xmldom');
const { captureRejectionSymbol } = require('events');

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
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    Weight: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    Height: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    CaloriesBurned: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    Sleep: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    BloodPressure: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    BloodGlucose: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    BodyFatPercentage: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    BasalMetabolicRate: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    WalkingSpeed: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    Distance: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    }
}

let groupPermissions = {
    AllVariables: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false,
        groupVariables: []
    },
    FitnessVariables: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        // we'll use these to know if we should set individual permissions or not
        // e.g. when checking HeartRate, if all healthVariables were already set, we don't need to add it again
        wasSet: false,
        groupVariables: ["Steps", "CaloriesBurned", "WalkingSpeed", "Distance"]
    },
    HealthVariables: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false,
        groupVariables: ["Steps", "CaloriesBurned", "WalkingSpeed", "Distance"]
    },
    ProfileVariables: {
        variableName: "HeartRate",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false,
        groupVariables: ["Steps", "CaloriesBurned", "WalkingSpeed", "Distance"]
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

    permissions.map((p) => {
        p.configValue = configParser.getPlatformPreference(p.variableName, 'android');
    })

    groupPermissions.map((p) => {
        p.configValue = configParser.getPlatformPreference(p.variableName, 'android');
    })

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
    Object.entries(permissions).forEach( p => {
        if (p.configValue == READWRITE || p.configValue == READ) {
            p.wasSet = true;
            processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, p.readPermission)
        }
        if (p.configValue == READWRITE || p.configValue == WRITE) {
            p.wasSet = true;
            processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, p.writePermission)
        }
    })


    // process group variables
    Object.entries(groupPermissions).forEach( p => {
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
    })

    // process AllVariables
    Object.entries(groupPermissions).forEach( p => {
        p.groupVariables.forEach( v => {
            if (allVariables == READWRITE || allVariables == READ) {
                if (!p.wasSet && !permissions[v].wasSet) {
                    processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, permissions[v].readPermission)
                }
            }
            if ((allVariables == READWRITE || allVariables == WRITE)) {
                if (!p.wasSet && !permissions[v].wasSet) {
                    processPermission(manifestXmlDoc, permissionsXmlDoc, arrayElement, permissions[v].writePermission)
                }
            }

            
        })
    })

    // process AllVariables
    if (groupPermissions.AllVariables.configValue == READWRITE || groupPermissions.AllVariables.configValue == READ) {   
        processAllVariables(manifestXmlDoc, permissionsXmlDoc, arrayElement, READ)

    }

    if ((groupPermissions.AllVariables.configValue == READWRITE || groupPermissions.AllVariables.configValue == WRITE)) {  
        processAllVariables(manifestXmlDoc, permissionsXmlDoc, arrayElement, WRITE)
    }
    

    let numberOfPermissions = permissions.filter(p => p.configValue != "").length + groupPermissions.filter(p => p.configValue != "").length
    // if there is no AllVariables nor anything else, then by default we add all the permissions
    if (numberOfPermissions == 0) {
        Object.entries(permissions).forEach( p => {
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

function processAllVariables(manifestXmlDoc, permissionsXmlDoc, arrayElement, permissionOperation) {
    Object.entries(groupPermissions).forEach(p => {
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
