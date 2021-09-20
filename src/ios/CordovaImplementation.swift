
import Foundation

class CordovaImplementation: CDVPlugin, IOSPlatformInterface {
    
    func sendResult(result: String, error: NSError?, callBackID:String) {
            var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        
        guard let error = error else {
            return
        }
            
        if !error.localizedDescription.isEmpty {
            let errorCode = String(error.code)
            let errorMessage = error.localizedDescription
            let errorDict = ["code": errorCode, "message": errorMessage]
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
    func sendResult(result: String, error: NSError?, callBackID:String) {
    
    }
    
}
