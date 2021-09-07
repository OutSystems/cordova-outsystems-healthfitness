import Foundation
import UIKit

@objc(OSHealthFitness)
class OSHealthFitness: CDVPlugin {
    var plugin: HealthFitnessPlugin?
    var callbackId:String=""
    
    override func pluginInitialize() {
        plugin = HealthFitnessPlugin()
    }
    
    
    @objc(requestPermissions:)
    func requestPermissions(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
    
        
        do {
            try plugin?.requestPermissions()
        } catch {
            sendResult(result: "", error: error.localizedDescription)
        }
    }
    
    @objc(sendResult:error:)
        func sendResult(result:String,error:String) {
            var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
            
            if error.isEmpty {
                let resultArray = [result]
                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: resultArray)
            } else {
                let errorDict = ["code": "0", "message": error]
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorDict);
            }
            self.commandDelegate!.send(pluginResult, callbackId: callbackId);
        }
    
}
