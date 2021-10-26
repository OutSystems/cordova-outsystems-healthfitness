const fs = require('fs');
const child_process = require('child_process')

module.exports = function (context) {
    const projectRoot = context.opts.projectRoot;
    let pluginId = context.opts.plugin.id
    let pluginPath = "/plugins/" + pluginId
    let fileUrl = "https://raw.githubusercontent.com/OutSystems/os-plugins-base-interface/main/ios/CordovaImplementation.swift"
    let dest = projectRoot + pluginPath  + "/src/ios/CordovaImplementation.swift";
    fs.writeFileSync(dest, downloadFileSync(fileUrl));


};

function downloadFileSync(url) {
  return child_process.execFileSync('curl', ['--silent', '-L', url]);
}