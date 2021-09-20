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
                self.sendResult(result: "", error:err , callBackID: self.callbackId)
            }
            
            if authorized {
                self.sendResult(result: "", error: nil, callBackID: self.callbackId)
            }
        }
    }
    
    @objc(writeData:)
    func writeData(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        
        let variable = command.arguments[0] as? String ?? ""
        let value = command.arguments[1] as? String ?? ""
        
        plugin?.writeData(variable: variable, value: value) { success,error in
            if let err = error {
                self.sendResult(result: "", error:err, callBackID: self.callbackId)
            }
            if success {
                self.sendResult(result: "", error: nil, callBackID: self.callbackId)
            }
        }
    }
    
    @objc(getLastRecord:)
    func getLastRecord(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        
        let variable = command.arguments[0] as? String ?? ""
        
        plugin?.getLastRecord(variable: variable) { success, result, error in
            if let err = error {
                self.sendResult(result: "", error:err, callBackID: self.callbackId)
            }
            if let result = result {
                if success {
                    let dictResult = ["result": result]
                    self.sendResultAsDictionary(result: dictResult, error: nil, callBackID: self.callbackId)
                }
            }
        }
    }
    
    @objc(getData:)
    func getData(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        
        let queryParameters = command.arguments[0] as? String ?? ""
        if let params = queryParameters.decode(string: queryParameters) as QueryParameters? {
                
            let variable = params.variable ?? ""
            let startDate = params.startDate ?? ""
            let endDate = params.endDate ?? ""
            let timeUnit = params.timeUnit ?? ""
            let operationType = params.operationType ?? ""
                
            plugin?.getData(variable: variable,
                            startDate: Date(startDate),
                            endDate: Date(endDate),
                            timeUnit: timeUnit,
                            operationType: operationType ) { success, result, error in

                if error != nil {
                    self.sendResult(result: nil, error: error, callBackID: self.callbackId)
                }
                else if success {
                    self.sendResult(result: result, error: nil, callBackID: self.callbackId)
                }
            }
     
        }
            
    }
    
}
