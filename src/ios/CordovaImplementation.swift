
import Foundation

class CordovaImplementation: CDVPlugin, IOSPlatformInterface {
    
    func sendResult(result: String?, error: String?, callBackID:String) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        
        if error != nil {
            let errorDict = ["code": "0", "message": error]
            if let jsonData = try?  JSONSerialization.data(withJSONObject: errorDict, options: .prettyPrinted),
               let json = String(data: jsonData, encoding: String.Encoding.ascii) {
                print("JSON string = \n\(json)")
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: json);
            }
            
        } else {
            if let result = result {
                if !result.isEmpty {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result)
                } else {
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
                }
            }
        }
        
        self.commandDelegate!.send(pluginResult, callbackId: callBackID);
    }
}


class ReactNativeImplementation: IOSPlatformInterface {
    
    // for future implementations we can use react native
    func sendResult(result: String, error: String, callBackID:String) {
    
    }
    
}
