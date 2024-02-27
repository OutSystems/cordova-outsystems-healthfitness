"use strict";

const fs = require('fs');
const path = require('path');
const { ConfigParser } = require('cordova-common');
const et = require('elementtree');
const { fileExists } = require('./utils');
let fileNamePrivacyPolicy = "HealthConnect_PrivacyPolicy.txt";

module.exports = async function (context) {
    const projectRoot = context.opts.cordova.project ? context.opts.cordova.project.root : context.opts.projectRoot;
    const platformPath = path.join(projectRoot, `platforms/android/app/src/main/assets/www/${fileNamePrivacyPolicy}`);

    if (fileExists(platformPath)) {
        const configXML = path.join(projectRoot, 'config.xml');
        const configParser = new ConfigParser(configXML);
        
        setPrivacyPolicyUrl(configParser, projectRoot);
    } else {
        throw new Error("Privacy Policy file not found in the resources folder.");
    }
};

function setPrivacyPolicyUrl(configParser, projectRoot) {
    const hostname = configParser.getPreference('hostname', 'android');
    const applicationNameUrl = configParser.getPreference('DefaultApplicationURL', 'android');
    
    if (hostname && applicationNameUrl) {
        const url = `https://${hostname}/${applicationNameUrl}/${fileNamePrivacyPolicy}`;
        const stringsPath = path.join(projectRoot, 'platforms/android/app/src/main/res/values/strings.xml');
        const stringsFile = fs.readFileSync(stringsPath).toString();
        const etreeStrings = et.parse(stringsFile);
    
        let privacyPolicyUrl = etreeStrings.find('./string[@name="privacy_policy_url"]');
        if (!privacyPolicyUrl) {
            console.error('Privacy policy URL string not found in strings.xml');
            return;
        }
        privacyPolicyUrl.text = url;
        const resultXmlStrings = etreeStrings.write({method: 'xml'});
        fs.writeFileSync(stringsPath, resultXmlStrings);
    } else {
        throw new Error("Error getting the environment variables.");
    }
}
