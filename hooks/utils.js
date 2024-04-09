"use strict"

var fs = require("fs");

function fileExists(filePath) {
    return fs.existsSync(filePath);
}

module.exports = {
    fileExists
};
