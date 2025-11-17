"use strict";

const fs = require('fs');
const path = require('path');
const { ConfigParser } = require('cordova-common');
const et = require('elementtree');
const { fileExists } = require('./utils');

let fileNamePrivacyPolicy = "HealthConnect_PrivacyPolicy.txt";
let mainFolder = "platforms/android/app/src/main/";

module.exports = async function (context) {
    const projectRoot = context.opts.cordova.project ? context.opts.cordova.project.root : context.opts.projectRoot;
    const directoryPath = path.join(projectRoot, mainFolder);
    const platformPath = path.join(directoryPath, `assets/www/${fileNamePrivacyPolicy}`);

    if (fileExists(platformPath) || policyFileExists(directoryPath)) {
        const configXML = path.join(projectRoot, 'config.xml');
        const configParser = new ConfigParser(configXML);
        
        setPrivacyPolicyUrl(configParser, projectRoot);
    } else {
        throw new Error("OUTSYSTEMS_PLUGIN_ERROR: Privacy Policy file not found in the resources folder.");
    }
};

function setPrivacyPolicyUrl(configParser, projectRoot) {
    const hostname = configParser.getPreference('hostname', 'android');
    const applicationNameUrl = configParser.getPreference('DefaultApplicationURL', 'android');
    
    if (hostname && applicationNameUrl) {
        const url = `https://${hostname}/${applicationNameUrl}/${fileNamePrivacyPolicy}`;
        const stringsPath = path.join(projectRoot, mainFolder, 'res/values/os_healthfitness_strings.xml');
        const stringsFile = fs.readFileSync(stringsPath).toString();
        const etreeStrings = et.parse(stringsFile);
        const resources = etreeStrings.getroot();

        // find all matching elements
        const existingEntries = resources.findall('./string[@name="privacy_policy_url"]');

        if (existingEntries.length > 0) {
            // update the first match
            existingEntries[0].text = url;

            // remove any extra duplicates (if they exist)
            for (let i = 1; i < existingEntries.length; i++) {
                resources.remove(existingEntries[i]);
            }
        } else {
            // create a new <string> element for privacy_policy_url if it does not exist
            const newElement = new et.Element('string');
            newElement.set('name', 'privacy_policy_url');
            newElement.text = url;
            resources.append(newElement);
        }

        const resultXmlStrings = etreeStrings.write({method: 'xml'});
        fs.writeFileSync(stringsPath, resultXmlStrings);
    } else {
        throw new Error(`OUTSYSTEMS_PLUGIN_ERROR: Error getting the environment variables.`);
    }
}

function policyFileExists(platformPath) {
    const directoryPath = path.join(platformPath, 'assets/www');
    // splits the file in name & format.
    const searchStrings = fileNamePrivacyPolicy.split('.');

    try {
        const files = fs.readdirSync(directoryPath);
        const matchingFiles = files.filter(fileName => fileName.startsWith(searchStrings[0]) && fileName.endsWith(searchStrings[1]));
        
        // return true if there are matching files, false otherwise
        return matchingFiles.length > 0;
    } catch (error) {
        console.error(error);
        throw new Error(`OUTSYSTEMS_PLUGIN_ERROR: An expected error occurred - Please check the logs for more information.`);
    }
}