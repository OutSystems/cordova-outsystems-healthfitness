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

// Health permission constants
const READ = "Read";
const WRITE = "Write";
const READWRITE = "ReadWrite";

// Individual permissions mapping
const permissions = {
    HeartRate: {
        configKey: "HEART_RATE",
        readPermission: "android.permission.health.READ_HEART_RATE",
        writePermission: "android.permission.health.WRITE_HEART_RATE",
        configValue: undefined,
        wasSet: false
    },
    Steps: {
        configKey: "STEPS",
        readPermission: "android.permission.health.READ_STEPS",
        writePermission: "android.permission.health.WRITE_STEPS",
        configValue: undefined,
        wasSet: false
    },
    Weight: {
        configKey: "WEIGHT",
        readPermission: "android.permission.health.READ_WEIGHT",
        writePermission: "android.permission.health.WRITE_WEIGHT",
        configValue: undefined,
        wasSet: false
    },
    Height: {
        configKey: "HEIGHT",
        readPermission: "android.permission.health.READ_HEIGHT",
        writePermission: "android.permission.health.WRITE_HEIGHT",
        configValue: undefined,
        wasSet: false
    },
    CaloriesBurned: {
        configKey: "CALORIES_BURNED",
        readPermission: "android.permission.health.READ_TOTAL_CALORIES_BURNED",
        writePermission: "android.permission.health.WRITE_TOTAL_CALORIES_BURNED",
        configValue: undefined,
        wasSet: false
    },
    Sleep: {
        configKey: "SLEEP",
        readPermission: "android.permission.health.READ_SLEEP",
        writePermission: "android.permission.health.WRITE_SLEEP",
        configValue: undefined,
        wasSet: false
    },
    BloodPressure: {
        configKey: "BLOOD_PRESSURE",
        readPermission: "android.permission.health.READ_BLOOD_PRESSURE",
        writePermission: "android.permission.health.WRITE_BLOOD_PRESSURE",
        configValue: undefined,
        wasSet: false
    },
    BloodGlucose: {
        configKey: "BLOOD_GLUCOSE",
        readPermission: "android.permission.health.READ_BLOOD_GLUCOSE",
        writePermission: "android.permission.health.WRITE_BLOOD_GLUCOSE",
        configValue: undefined,
        wasSet: false
    },
    BodyFatPercentage: {
        configKey: "BODY_FAT_PERCENTAGE",
        readPermission: "android.permission.health.READ_BODY_FAT",
        writePermission: "android.permission.health.WRITE_BODY_FAT",
        configValue: undefined,
        wasSet: false
    },
    BasalMetabolicRate: {
        configKey: "BASAL_METABOLIC_RATE",
        readPermission: "android.permission.health.READ_BASAL_METABOLIC_RATE",
        writePermission: "android.permission.health.WRITE_BASAL_METABOLIC_RATE",
        configValue: undefined,
        wasSet: false
    },
    WalkingSpeed: {
        configKey: "WALKING_SPEED",
        readPermission: "android.permission.health.READ_SPEED",
        writePermission: "android.permission.health.WRITE_SPEED",
        configValue: undefined,
        wasSet: false
    },
    Distance: {
        configKey: "DISTANCE",
        readPermission: "android.permission.health.READ_DISTANCE",
        writePermission: "android.permission.health.WRITE_DISTANCE",
        configValue: undefined,
        wasSet: false
    },
    OxygenSaturation: {
        configKey: "OXYGEN_SATURATION",
        readPermission: "android.permission.health.READ_OXYGEN_SATURATION",
        writePermission: "android.permission.health.WRITE_OXYGEN_SATURATION",
        configValue: undefined,
        wasSet: false
    },
    BodyTemperature: {
        configKey: "BODY_TEMPERATURE",
        readPermission: "android.permission.health.READ_BODY_TEMPERATURE",
        writePermission: "android.permission.health.WRITE_BODY_TEMPERATURE",
        configValue: undefined,
        wasSet: false
    }
};

// Group permissions mapping
const groupPermissions = {
    AllVariables: {
        configKey: "ALL_VARIABLES",
        configValue: undefined,
        wasSet: false,
        groupVariables: []
    },
    FitnessVariables: {
        configKey: "FITNESS_VARIABLES",
        configValue: undefined,
        wasSet: false,
        groupVariables: ["Steps", "CaloriesBurned", "WalkingSpeed", "Distance"]
    },
    HealthVariables: {
        configKey: "HEALTH_VARIABLES",
        configValue: undefined,
        wasSet: false,
        groupVariables: ["HeartRate", "Sleep", "BloodPressure", "BloodGlucose", "OxygenSaturation", "BodyTemperature"]
    },
    ProfileVariables: {
        configKey: "PROFILE_VARIABLES",
        configValue: undefined,
        wasSet: false,
        groupVariables: ["Weight", "Height", "BodyFatPercentage", "BasalMetabolicRate"]
    }
};

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

function getHealthFitnessConfig() {
    try {
        const configPath = path.join(projectRoot, "healthfitness.config.json");
        
        if (fs.existsSync(configPath)) {
            const configContent = fs.readFileSync(configPath, "utf8");
            return JSON.parse(configContent);
        }
        
        console.log("HealthFitness: No healthfitness.config.json file found");
        return null;
    } catch (err) {
        console.log("HealthFitness: Could not read healthfitness config:", err.message);
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

function getConfigValue(config, key) {
    if (!config || !config.permissions) {
        return undefined;
    }
    return config.permissions[key];
}

function getGroupConfigValue(config, key) {
    if (!config || !config.groupPermissions) {
        return undefined;
    }
    return config.groupPermissions[key];
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

function addHealthConnectPermissions(config) {
    const parser = new DOMParser();
    
    // Get individual permission values from config
    for(const key in permissions){
        permissions[key].configValue = getConfigValue(config, permissions[key].configKey);
    }

    // Get group permission values from config  
    for(const key in groupPermissions){
        groupPermissions[key].configValue = getGroupConfigValue(config, groupPermissions[key].configKey);
    }

    // Read AndroidManifest.xml
    const manifestFilePath = path.join(getAppDir(), 'app/src/main/AndroidManifest.xml');
    if (!fs.existsSync(manifestFilePath)) {
        console.log('HealthFitness: AndroidManifest.xml not found, skipping permission setup');
        return;
    }
    
    const manifestXmlString = fs.readFileSync(manifestFilePath, 'utf-8');
    const manifestXmlDoc = parser.parseFromString(manifestXmlString, 'text/xml');

    // Process individual permissions
    for(const key in permissions){
        let p = permissions[key];
        if (p.configValue == READWRITE || p.configValue == READ) {
            p.wasSet = true;
            addEntryToManifest(manifestXmlDoc, p.readPermission);
        }
        if (p.configValue == READWRITE || p.configValue == WRITE) {
            p.wasSet = true;
            addEntryToManifest(manifestXmlDoc, p.writePermission);
        }
    }

    // Process group permissions
    for(const key in groupPermissions){
        let p = groupPermissions[key];
        if (p.configValue == READWRITE || p.configValue == READ) {
            p.wasSet = true;
            p.groupVariables.forEach( v => {
                if (!permissions[v].wasSet) {
                    addEntryToManifest(manifestXmlDoc, permissions[v].readPermission);
                }
            });
        }
        if (p.configValue == READWRITE || p.configValue == WRITE) {
            p.wasSet = true;
            p.groupVariables.forEach( v => {
                if (!permissions[v].wasSet) {
                    addEntryToManifest(manifestXmlDoc, permissions[v].writePermission);
                }
            });
        }
    }

    // Process AllVariables
    if (groupPermissions.AllVariables.configValue == READWRITE || groupPermissions.AllVariables.configValue == READ) {   
        processAllVariables(manifestXmlDoc, READ, Object.values(groupPermissions));
    }
    if (groupPermissions.AllVariables.configValue == READWRITE || groupPermissions.AllVariables.configValue == WRITE) {  
        processAllVariables(manifestXmlDoc, WRITE, Object.values(groupPermissions));
    }
    
    // Check if any permissions were set
    let numberOfPermissions = Object.values(permissions).filter(p => p.configValue && p.configValue !== "").length + 
                             Object.values(groupPermissions).filter(p => p.configValue && p.configValue !== "").length;

    // If no permissions set, add all by default (matching Cordova behavior)
    if (numberOfPermissions == 0) {
        Object.values(permissions).forEach( p => {
            addEntryToManifest(manifestXmlDoc, p.readPermission);
            addEntryToManifest(manifestXmlDoc, p.writePermission);
        });
    }

    // Write updated files
    const serializer = new XMLSerializer();
    
    // Update AndroidManifest.xml
    const updatedManifestXmlString = serializer.serializeToString(manifestXmlDoc);
    fs.writeFileSync(manifestFilePath, updatedManifestXmlString, 'utf-8');
    
    console.log('HealthFitness: Health permissions configured successfully');
}

function processAllVariables(manifestXmlDoc, permissionOperation, groupPermissionsValues) {
    groupPermissionsValues.forEach(p => {
        p.groupVariables.forEach( v => {
            if (!p.wasSet && !permissions[v].wasSet) {
                addEntryToManifest(manifestXmlDoc, permissionOperation == READ ? permissions[v].readPermission : permissions[v].writePermission);
            }
        });
    });  
}

function addEntryToManifest(manifestXmlDoc, permission) {
    // Check if permission already exists
    const existingPermissions = manifestXmlDoc.getElementsByTagName('uses-permission');
    for (let i = 0; i < existingPermissions.length; i++) {
        if (existingPermissions[i].getAttribute('android:name') === permission) {
            return; // Permission already exists
        }
    }

    const indent = manifestXmlDoc.createTextNode('    ');
    manifestXmlDoc.documentElement.appendChild(indent);

    const newPermission = manifestXmlDoc.createElement('uses-permission');
    newPermission.setAttribute('android:name', permission);
    manifestXmlDoc.documentElement.appendChild(newPermission);

    const newline = manifestXmlDoc.createTextNode('\n');
    manifestXmlDoc.documentElement.appendChild(newline);
}

function setPrivacyPolicyUrl(config) {
    let hostname, applicationNameUrl;

    if (config && config.server) {
        hostname = config.server.hostname ?? 
                   config.plugins?.OutSystemsCore?.defaultHostname ??  
                   "localhost";
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
    const config = getHealthFitnessConfig();
    const appDir = getAppDir();
    const platformPath = path.join(appDir, 'app/src/main');
    const assetsPath = path.join(platformPath, `assets/public/${fileNamePrivacyPolicy}`);
    
    // Configure health permissions
    addHealthConnectPermissions(config);
    
    // Check if privacy policy file exists or if we should construct URL
    if (fs.existsSync(assetsPath) || policyFileExists(platformPath)) {
        setPrivacyPolicyUrl(config);
    } else {
        console.log('HealthFitness: Privacy Policy file not found, URL will need to be set via build action.');
    }
}

if (platform === "android") {
    configureAndroid();
}