
import Foundation

class CordovaImplementation: CDVPlugin, IOSPlatformInterface {
    
    func sendResult(result: String, error: String, callBackID:String) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        
        if !error.isEmpty {
            let errorDict = ["code": "0", "message": error]
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorDict);
        } else if result.isEmpty {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        } else {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result)
        }
        
        self.commandDelegate!.send(pluginResult, callbackId: callBackID);
    }
    
}


class ReactNativeImplementation: IOSPlatformInterface {
    
    // for future implementations we can use react native
    func sendResult(result: String, error: String, callBackID:String) {
    
    }
    
}
