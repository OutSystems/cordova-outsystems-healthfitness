const path = require("path");
const fs = require("fs");
const { DOMParser, XMLSerializer } = require('@xmldom/xmldom');
const babel = require('@babel/core');

const projectRoot = process.env.CAPACITOR_ROOT_DIR;
const platform = process.env.CAPACITOR_PLATFORM_NAME;

if (!platform || !projectRoot) {
    throw new Error("OUTSYSTEMS_PLUGIN_ERROR: Missing required environment variables.");
}

const fileNamePrivacyPolicy = "HealthConnect_PrivacyPolicy.txt";

function getAppDir() {
    return path.join(projectRoot, "android");
}

function getCapacitorConfig() {
    try {
        // Try to read capacitor.config.ts first, then capacitor.config.js, then capacitor.config.json
        const tsConfigPath = path.join(projectRoot, "capacitor.config.ts");
        const jsConfigPath = path.join(projectRoot, "capacitor.config.js");
        const jsonConfigPath = path.join(projectRoot, "capacitor.config.json");
        
        if (fs.existsSync(tsConfigPath)) {
            return parseConfigFile(tsConfigPath);
        } else if (fs.existsSync(jsConfigPath)) {
            return parseConfigFile(jsConfigPath);
        } else if (fs.existsSync(jsonConfigPath)) {
            const configContent = fs.readFileSync(jsonConfigPath, "utf8");
            return JSON.parse(configContent);
        }
        
        console.log("HealthFitness: No Capacitor config file found");
        return null;
    } catch (err) {
        console.log("HealthFitness: Could not read Capacitor config:", err.message);
        return null;
    }
}

function parseConfigFile(configPath) {
    try {
        const configContent = fs.readFileSync(configPath, "utf8");
        
        const result = babel.transformSync(configContent, {
            presets: [
                ['@babel/preset-typescript', { allowNamespaces: true }],
                ['@babel/preset-env', { targets: { node: 'current' } }]
            ],
            filename: configPath
        });
        
        const moduleContext = {
            exports: {},
            module: { exports: {} }
        };
        
        const func = new Function('exports', 'module', 'require', result.code);
        func(moduleContext.exports, moduleContext.module, require);
        return moduleContext.exports.default || moduleContext.module.exports.default || moduleContext.module.exports || moduleContext.exports;
    } catch (err) {
        console.log("HealthFitness: Failed to parse config file:", err.message);
        return null;
    }
}

function fileExists(filePath) {
    return fs.existsSync(filePath);
}

function policyFileExists(platformPath) {
    const directoryPath = path.join(platformPath, 'assets/public');
    const searchStrings = fileNamePrivacyPolicy.split('.');

    try {
        if (!fs.existsSync(directoryPath)) {
            return false;
        }
        const files = fs.readdirSync(directoryPath);
        const matchingFiles = files.filter(fileName => 
            fileName.startsWith(searchStrings[0]) && fileName.endsWith(searchStrings[1])
        );
        return matchingFiles.length > 0;
    } catch (error) {
        console.error("HealthFitness: Error checking policy file existence:", error);
        return false;
    }
}

function setPrivacyPolicyUrl(config) {
    let hostname, applicationNameUrl;

    if (config && config.server) {
        hostname = config.server.hostname ?? "localhost";
        applicationNameUrl = config.server.url;
    }

    if (hostname && applicationNameUrl) {
        // Clean up the applicationNameUrl if it contains the full URL
        if (applicationNameUrl.startsWith('http')) {
            try {
                const url = new URL(applicationNameUrl);
                applicationNameUrl = url.pathname.replace(/^\//, '').replace(/\/$/, '');
            } catch (e) {
                console.log("HealthFitness: Could not parse application URL, using as-is");
            }
        }
        
        const url = `https://${hostname}/${applicationNameUrl}/${fileNamePrivacyPolicy}`;
        const stringsPath = path.join(getAppDir(), 'app/src/main/res/values/strings.xml');
        
        if (!fs.existsSync(stringsPath)) {
            throw new Error(`OUTSYSTEMS_PLUGIN_ERROR: ${stringsPath} file not found.`);
        }
        
        try {
            const parser = new DOMParser();
            const stringsFile = fs.readFileSync(stringsPath, 'utf-8');
            const stringsDoc = parser.parseFromString(stringsFile, 'text/xml');
            
            const resourcesElement = stringsDoc.getElementsByTagName('resources')[0];
            if (!resourcesElement) {
                throw new Error('OUTSYSTEMS_PLUGIN_ERROR: No <resources> element found in strings.xml.');
            }
            
            // Helper function to find or create a string element
            function findOrCreateStringElement(name, defaultValue) {
                const stringElements = stringsDoc.getElementsByTagName('string');
                
                for (let i = 0; i < stringElements.length; i++) {
                    if (stringElements[i].getAttribute('name') === name) {
                        return stringElements[i];
                    }
                }
                
                // Create the element if it doesn't exist
                const newStringElement = stringsDoc.createElement('string');
                newStringElement.setAttribute('name', name);
                newStringElement.textContent = defaultValue;
                
                const indent = stringsDoc.createTextNode('    ');
                resourcesElement.appendChild(indent);
                resourcesElement.appendChild(newStringElement);
                
                console.log(`HealthFitness: Created missing string element: ${name}`);
                return newStringElement;
            }
            
            // Ensure all required string elements exist
            const privacyPolicyElement = findOrCreateStringElement('privacy_policy_url', 'PRIVACY_POLICY_URL');
            
            // Only update privacy policy URL if it's still the placeholder value (meaning build action didn't override it)
            if (privacyPolicyElement.textContent === 'PRIVACY_POLICY_URL') {
                privacyPolicyElement.textContent = url;
                
                const serializer = new XMLSerializer();
                let updatedXmlString = serializer.serializeToString(stringsDoc);
                updatedXmlString = updatedXmlString.replace(/<\/resources>$/, '\n</resources>\n');
                
                fs.writeFileSync(stringsPath, updatedXmlString, 'utf-8');
                
                console.log(`HealthFitness: Privacy policy URL set to: ${url}`);
            } else {
                console.log('HealthFitness: Privacy policy URL already set via build action, skipping.');
            }
        } catch (xmlError) {
            console.error('HealthFitness: Error updating strings.xml:', xmlError.message);
        }
    } else {
        console.log('HealthFitness: Could not determine hostname and application URL for privacy policy construction.');
        console.log(`HealthFitness: hostname=${hostname}, applicationNameUrl=${applicationNameUrl}`);
    }
}

function configureAndroid() {
    const capacitorConfig = getCapacitorConfig();
    const appDir = getAppDir();
    const platformPath = path.join(appDir, 'app/src/main');
    const assetsPath = path.join(platformPath, `assets/public/${fileNamePrivacyPolicy}`);
    
    // Check if privacy policy file exists or if we should construct URL
    if (fileExists(assetsPath) || policyFileExists(platformPath)) {
        setPrivacyPolicyUrl(capacitorConfig);
    } else {
        console.log('HealthFitness: Privacy Policy file not found, URL will need to be set via build action.');
    }
}

// Handle Android - Privacy Policy URL configuration
if (platform === "android") {
    configureAndroid();
}