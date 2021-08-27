var XMLHttpRequest = require("xmlhttprequest").XMLHttpRequest;

if(process.env.npm_package_config_applicationNames == null) {
    throw new Error("Missing applicationNames array configuration in package.json");
}

if(process.env.npm_config_fromEnvironment == null || process.env.npm_config_toEnvironment == null) {
    throw new Error("Missing repositoryURL, branch, environment arguments");
}

if(process.env.npm_config_authentication == null) {
    throw new Error("Missing authentication argument");
}

if(process.env.npm_config_pipelineSupportURL == null) {
    throw new Error("The base pipeline support app URL \"pipelineSupportURL\" not set");
}

if(process.env.npm_config_destinationFolder == null) {
    throw new Error("The destination folder path for the ipa/apks not set \"destinationFolder\"");
}

var fromEnvironment = process.env.npm_config_fromEnvironment;
var toEnvironment = process.env.npm_config_toEnvironment;
var applicationNames = process.env.npm_package_config_applicationNames;
var basicAuthentication = process.env.npm_config_authentication;
var baseURL = process.env.npm_config_pipelineSupportURL;
var downloadDestinationFolder = process.env.npm_config_destinationFolder;

console.log("Start deployment");

var deploymentKey = deploy(baseURL, fromEnvironment, toEnvironment, applicationNames, basicAuthentication);
var pipelineStatus = "";
var lastLog = {"Instant":"", "Message": ""};
var buildProgress = {};
var checkResponse = null;

while(checkCondition(deploymentStatus)) {
	console.log("Checking Status");
	checkResponse = checkStatus(baseURL, deploymentKey, basicAuthentication);
	var newStatus = checkResponse.Status;

	if(pipelineStatus !=  newStatus) {
		pipelineStatus = newStatus;
		console.log("Status is:" + pipelineStatus);
		console.log("");
	}

	if(pipelineStatus=="StartedDeployment" || pipelineStatus=="Success") {
		if(process.env.npm_config_verbose == null) {
			var newLastLog = checkResponse.DeploymentLog[checkResponse.DeploymentLog.length - 1];
			if(lastLog.Instant != newLastLog.Instant) {
				lastLog = newLastLog;
				console.log("Deployment log message date: " + lastLog.Instant + "|| Message: " + lastLog.Message);
			}
		} else {
			checkResponse.DeploymentLog.forEach( (element) => printBuilding(element) );
		}
	}

	if(pipelineStatus=="StartedBuilding" || pipelineStatus=="Success") {
		checkResponse.BuildingResponse.ApplicationPlatform.forEach( (element) => console.log("Date: " + element.Instant + "|| Message: " + element.Message) );
	}

	if(pipelineStatus=="Fail") {
		throw new Error("Message: " + checkResponse.ErrorResponse.Error);
	}
	if(checkCondition(pipelineStatus)) {
		sleep(5000);
	}
}

var downloadsBaseURL = checkResponse.SuccessResponse.baseURL;
var iosPath = checkResponse.SuccessResponse.iosPath;
var androidPath = checkResponse.SuccessResponse.androidPath;

console.log("Starting download of apps");

checkResponse.SuccessResponse.SuccessResponse.downloadableApplications.forEach(function(element) {
	var iosURL = downloadsBaseURL + iosPath + "?appKey=" + element.appKey;
	var androidURL = downloadsBaseURL + androidPath + "?appKey=" + element.appKey;

	console.log("Apps will be available in: " + downloadDestinationFolder);
	console.log("Started download of ios app for " + element.appName);
	
	download(iosURL, downloadDestinationFolder + element.appName, "ios-app.ipa", function(error) {
		if(error == null) {
			console.log("Download success for ios platform of app " + element.appName);
		} else {
			console.log("Error downloading android platform of app " + element.appName + " with error: " + error);
		}
	});

	console.log("Started download of android app for " + element.appName);
	download(androidURL, downloadDestinationFolder + element.appName, "android-app.apk", function(error) {
		if(error == null) {
			console.log("Download success for android platform of app " + element.appName);
		} else {
			console.log("Error downloading android platform of app " + element.appName + " with error: " + error);
		}
	});
});

function checkCondition(status) {
	return !(status.includes("Success") || status.includes("Fail"));
}

function deploy(base, fromEnv, toEnv, appNames, basicAuth) {
	var url =  base + "tagDeployBuild";
	var body = {
	    "fromEnvironmentName": fromEnv,
	    "toEnvironmentName": toEnv,
	    "applicationNames": appNames
	};

	var request = new XMLHttpRequest();
	request.open("POST", url, false);
	request.setRequestHeader("Authorization", basicAuth);
	request.setRequestHeader("Content-Type", "application/json");
	request.send(JSON.stringify(body));

	if(request.status == 200) {
	    console.log("Deployment Response:" + request.responseText);
	    return request.responseText;
	} else {
	    console.log("Network Error:", request);
	    console.log("Network Error:", request.statusText);
	    throw new Error("Message: " + request.responseText);
	}
}

function checkStatus(base, deployKey, basicAuth) {
	var url = base + "status?processKey=" + deployKey;

	var request = new XMLHttpRequest();
	request.open("GET", url, false);
	request.setRequestHeader("Authorization", basicAuth);
	request.setRequestHeader("Content-Type", "application/json");
	request.send();

	if(request.status == 200) {
	    return JSON.parse(request.responseText);
	} else {
	    console.log("Network Error:", request);
	    console.log("Network Error:", request.statusText);
	    throw new Error("Message: " + request.responseText);
	}
}

function printBuilding(element) {
	console.log("Application: " + element.Name)
	element.PlatformProgress.forEach( (platform) => console.log(platform.Name +  "Progress: " + element.Progress + "%") );
}

function sleep(milliseconds) {
  const date = Date.now();
  let currentDate = null;
  do {
    currentDate = Date.now();
  } while (currentDate - date < milliseconds);
}

var download = function(url, dest, filename, cb) {
	var file = fs.createWriteStream(dest + filename);
	var options = {
		headers: {
			"Authorization": basicAuth
		}
	};
	var request = http.request(url, options, function(response) {
	  response.pipe(file);
	  file.on('finish', function() {
		file.close(cb);
	  });
	}).on('error', function(err) {
	  fs.unlink(dest + filename); 
	  if (cb) cb(err.message);
	});
  };