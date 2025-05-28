const fs = require('fs');
const path = require('path');

const platform = process.env.CAPACITOR_PLATFORM_NAME;
console.log("\tHealthFitness plugin - running hook after update - for " + platform);
const projectDirPath = process.env.CAPACITOR_ROOT_DIR;

if (platform == 'android') {
    fixAndroidKaptGradleCapacitor();
}

function fixAndroidKaptGradleCapacitor() {
    const gradleFilePath = path.resolve(projectDirPath, 'android/app/build.gradle');
    const kaptPluginLong = 'org.jetbrains.kotlin.kapt';
    const kaptPluginShort = 'kotlin-kapt';
    const linesToPrepend = `
// region Kapt Plugin
// The lines inside this region were added via the HealthFitness Plugin to ensure kapt works in a Capacitor app.
apply plugin: 'kotlin-android'
apply plugin: 'kotlin-kapt'
// endregion
`.trimStart();

    if (!fs.existsSync(gradleFilePath)) {
        console.log('\t[SKIPPED] build.gradle file not found. Skipping KAPT fix.');
        return;
    }

    let gradleContent = fs.readFileSync(gradleFilePath, 'utf8');

    if (gradleContent.includes(kaptPluginLong) || gradleContent.includes(kaptPluginShort)) {
        console.log('\t[SKIPPED] Kotlin Gradle plugin already defined. Skipping update.');
    } else {
        const updatedContent = `${linesToPrepend}\n${gradleContent}`;
        fs.writeFileSync(gradleFilePath, updatedContent, 'utf8');
        console.log('\t[SUCCESS] Prepended Kotlin Kapt plugin to build.gradle');
    }
} 