import Foundation
import OSCore

class CordovaImplementation: CDVPlugin, PlatformProtocol {

    func sendResult(result: String?, error: NSError?, callBackID:String) {
            var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        
        if let error = error {
            if !error.localizedDescription.isEmpty {
                let errorCode = String(error.code)
                let errorMessage = error.localizedDescription
                let errorDict = ["code": errorCode, "message": errorMessage]
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorDict);
            }
        } else if let result = result {
            if result.isEmpty {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
            } else {
                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result)
            }
        }
        
        self.commandDelegate!.send(pluginResult, callbackId: callBackID);
    }
    
}


class ReactNativeImplementation: PlatformProtocol {
    
    // for future implementations we can use react native
    func sendResult(result: String?, error: NSError?, callBackID:String) {
    
    }
    
}
