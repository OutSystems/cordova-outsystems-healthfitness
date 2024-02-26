const fs = require('fs');
const path = require('path');
const { ConfigParser } = require('cordova-common');
const et = require('elementtree');
const { DOMParser, XMLSerializer } = require('xmldom');

module.exports = async function (context) {
    const projectRoot = context.opts.cordova.project ? context.opts.cordova.project.root : context.opts.projectRoot;

    const configXML = path.join(projectRoot, 'config.xml');
    const configParser = new ConfigParser(configXML);

    console.log('About to call addPermissionsToManfiest');
        
    addPermissionsToManifest(configParser, projectRoot);


};

function addPermissionsToManifest(configParser, projectRoot) {

    //get all the preferences from config.xml, for every Health Connect permission
    const allVariables = configParser.getPlatformPreference('AllVariables', 'android');
    const fitnessVariables = configParser.getPlatformPreference('FitnessVariables', 'android');
    const healthVariables = configParser.getPlatformPreference('HealthVariables', 'android');
    const profileVariables = configParser.getPlatformPreference('ProfileVariables', 'android');

    console.log('About to read the AndroidManifest.xml file');

    // Read the AndroidManifest.xml file
    const manifestFilePath = path.join(projectRoot, 'platforms/android/app/src/main/AndroidManifest.xml');
    const manifestXmlString = fs.readFileSync(manifestFilePath, 'utf-8');

    console.log('About to parse the XML string');

    // Parse the XML string
    const parser = new DOMParser();
    const manifestXmlDoc = parser.parseFromString(manifestXmlString, 'text/xml');

    console.log('About to append permission');

    const newPermission = manifestXmlDoc.createElement('uses-permission');
    newPermission.setAttribute('android:name', 'android.permission.health.READ_HEART_RATE');
    manifestXmlDoc.documentElement.appendChild(newPermission);

    console.log('About to serialize');

    // Serialize the updated XML document back to string
    const serializer = new XMLSerializer();
    const updatedManifestXmlString = serializer.serializeToString(manifestXmlDoc);

    console.log('About to write the file again');

    // Write the updated XML string back to the same file
    fs.writeFileSync(manifestFilePath, updatedManifestXmlString, 'utf-8');

    /*

    if (allVariables == "true") {
        // in this case, we don't look for any other preferences
        // add all the permissions for all the variables we have
        
    } else if (fitnessVariables == "true") {
        // add all permissions for fitness variables

    } else if (healthVariables == "true") {
        // add all permissions for health variables

    } else if (profileVariables == "true") {
        // add all permissions for profile variables

    } else {
        // look for every variable individually and add the permission for it if its value is true

    }

    */

    // for every permission that is present in the config.xml file, we add it to the AndroidManifest.xml file

    // finally, we save the file by doing the following
    
    /*
    let resultXmlStrings = etreeStrings.write();
    fs.writeFileSync(stringsPath, resultXmlStrings);
    */


}
