import Foundation

@objc(OSHealthFitness)
class OSHealthFitness: CordovaImplementation {
    var plugin: HealthFitnessPlugin?
    var callbackId:String=""
    
    override func pluginInitialize() {
        plugin = HealthFitnessPlugin()
    }
    
    @objc(requestPermissions:)
    func requestPermissions(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        
        let customPermissions = command.arguments[0] as? String ?? ""
        let allVariables = command.arguments[1] as? String ?? ""
        let fitnessVariables = command.arguments[2] as? String ?? ""
        let healthVariables = command.arguments[3] as? String ?? ""
        let profileVariables = command.arguments[4] as? String ?? ""
        let summaryVariables = command.arguments[5] as? String ?? ""
        
        plugin?.requestPermissions(customPermissions:customPermissions,
                                   allVariables:allVariables,
                                   fitnessVariables:fitnessVariables,
                                   healthVariables:healthVariables,
                                   profileVariables:profileVariables,
                                   summaryVariables:summaryVariables) { [self] (authorized, error) in
            
            if let err = error {
                self.sendResult(result: "", error:err.localizedDescription , callBackID: self.callbackId)
            }
            
            if authorized {
                self.sendResult(result: "", error: "", callBackID: self.callbackId)
            }
        }
    }
    
    @objc(getData:)
    func getData(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
    
        if let resultStr = plugin?.getData() {
            self.sendResult(result: resultStr, error: "", callBackID: callbackId)
        } else {
            self.sendResult(result: "", error: "Data is empty", callBackID: callbackId)
        }
        
    }
    
}
