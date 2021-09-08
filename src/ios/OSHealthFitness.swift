import Foundation

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
    
        plugin?.requestPermissions() { (authorized, error) in
            
            if authorized {
                self.sendResult(result: "", error: "")
            }
            
        }
    }
    
    
    @objc(getData:)
    func getData(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
    
        if let resultStr = plugin?.getData() {
            self.sendResult(result: resultStr, error: "")
        } else {
            self.sendResult(result: "", error: "Data is empty")
        }
        
    }
    
    @objc(sendResult:error:)
    func sendResult(result:String,error:String) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        
        if !result.isEmpty {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result)
        } else {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        }
        
        if !error.isEmpty {
            let errorDict = ["code": "0", "message": error]
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorDict);
        }
        
        self.commandDelegate!.send(pluginResult, callbackId: callbackId);
    }
    
}
