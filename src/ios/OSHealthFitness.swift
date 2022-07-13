import Foundation
import OSHealthFitnessLib

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
        
        guard let variable = command.arguments[0] as? String else {
            return self.sendResult(result: "", error:HealthKitErrors.badParameterType as NSError, callBackID: self.callbackId)
        }
        
        guard let value = command.arguments[1] as? Double else {
            return  self.sendResult(result: "", error:HealthKitErrors.badParameterType as NSError, callBackID: self.callbackId)
        }
        
        plugin?.writeData(variable: variable, value: value) { success,error in
            if let err = error {
                self.sendResult(result: "", error:err, callBackID: self.callbackId)
            }
            if success {
                self.sendResult(result: "", error: nil, callBackID: self.callbackId)
            }
        }
    }
    
    @objc(updateBackgroundJob:)
    func updateBackgroundJob(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        
        let queryParameters = command.arguments[0] as? String ?? ""
        if let parameters = parseUpdateParameters(parameters: queryParameters) {
            
            plugin?.updateBackgroundJob(id: parameters.id,
                                        notificationFrequency: parameters.notificationFrequency,
                                        notificationFrequencyGrouping: parameters.notificationFrequencyGrouping,
                                        condition: parameters.condition,
                                        value: parameters.value,
                                        notificationHeader: parameters.notificationHeader,
                                        notificationBody: parameters.notificationBody,
                                        isActive: parameters.isActive)
            { success, error in
                
                if error != nil {
                    self.sendResult(result: "", error: error, callBackID: self.callbackId)
                }
                else if success {
                    self.sendResult(result: "", error: nil, callBackID: self.callbackId)
                }
            }
            
        }
    }
    
    private func parseUpdateParameters(parameters: String) -> BackgroundJobParameters? {
        let data = parameters.data(using: .utf8)!
        if let jsonData = try? JSONSerialization.jsonObject(with: data, options : .allowFragments) as? Dictionary<String,Any> {
            
            // I'm doing this mess because Outsystems
            // seams to be sending the parameters as strings.
            let id = Int64(jsonData["Id"] as? String ?? "")
            let notificationFrequency = jsonData["NotificationFrequency"] as? String
            let notificationFrequencyGrouping = jsonData["NotificationFrequencyGrouping"] as? Int
            let condition = jsonData["Condition"] as? String
            let value = jsonData["Value"] as? Double
            let notificationHeader = jsonData["NotificationHeader"] as? String
            let notificationBody = jsonData["NotificationBody"] as? String
            var isActive: Bool? = nil
            let activeString = jsonData["IsActive"] as? String ?? ""
            if activeString != "" {
                isActive = activeString.lowercased() == "true"
            }
            
            return BackgroundJobParameters(id: id,
                                           variable: nil,
                                           timeUnit: nil,
                                           timeUnitGrouping: nil,
                                           notificationFrequency: notificationFrequency,
                                           notificationFrequencyGrouping: notificationFrequencyGrouping,
                                           jobFrequency: nil,
                                           condition: condition,
                                           value: value,
                                           notificationHeader: notificationHeader,
                                           notificationBody: notificationBody,
                                           isActive: isActive)
        }
        return nil
    }
    
    @objc(getLastRecord:)
    func getLastRecord(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        let variable = command.arguments[0] as? String ?? ""
        
        plugin?.advancedQuery(variable: variable,
                              startDate: Date.distantPast,
                              endDate: Date(),
                              timeUnit: "",
                              operationType: "MOST_RECENT",
                              mostRecent:true,
                              onlyFilledBlocks: false,
                              timeUnitLength: 1) { success, result, error in
            
            if error != nil {
                self.sendResult(result: nil, error: error, callBackID: self.callbackId)
            } else if success {
                self.sendResult(result: result, error: nil, callBackID: self.callbackId)
            }
        }
        
    }
    
    @objc(deleteBackgroundJob:)
    func deleteBackgroundJob(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        let id = command.arguments[0] as? String ?? ""
        plugin?.deleteBackgroundJobs(id: id) { success, error in
            if error != nil {
                self.sendResult(result: nil, error: error, callBackID: self.callbackId)
            } else if success {
                self.sendResult(result: "", error: nil, callBackID: self.callbackId)
            }
        }
    }
    
    @objc(listBackgroundJobs:)
    func listBackgroundJobs(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        plugin?.listBackgroundJobs() { success, result, error in
            if error != nil {
                self.sendResult(result: nil, error: error, callBackID: self.callbackId)
            } else if success {
                self.sendResult(result: result, error: nil, callBackID: self.callbackId)
            }
        }
    }
    
    @objc(setBackgroundJob:)
    func setBackgroundJob(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        
        let queryParameters = command.arguments[0] as? String ?? ""
        if let params = queryParameters.decode(string: queryParameters) as BackgroundJobParameters? {
            
            let variable = params.variable ?? ""
            let timeUnitGrouping = params.timeUnitGrouping ?? 0
            let condition = params.condition ?? ""
            let timeUnit = params.timeUnit ?? ""
            let jobFrequency = params.jobFrequency ?? ""
            let notificationFrequency = params.notificationFrequency ?? ""
            let notificationFrequencyGrouping = params.notificationFrequencyGrouping ?? 0
            let value = params.value ?? 0
            let notificationHeader = params.notificationHeader ?? ""
            let notificationBody = params.notificationBody ?? ""
            
            plugin?.setBackgroundJob(variable: variable,
                                     timeUnit: timeUnit,
                                     timeUnitGrouping: timeUnitGrouping,
                                     notificationFrequency: notificationFrequency,
                                     notificationFrequencyGrouping: notificationFrequencyGrouping,
                                     jobFrequency: jobFrequency,
                                     condition: condition,
                                     value: value,
                                     notificationHeader: notificationHeader,
                                     notificationBody: notificationBody)
            { success, result, error in
                
                if error != nil {
                    self.sendResult(result: nil, error: error, callBackID: self.callbackId)
                }
                else if success {
                    self.sendResult(result: result, error: nil, callBackID: self.callbackId)
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
            let timeUnitLength = params.timeUnitLength ?? 1
            let onlyFilledBlocks = params.advancedQueryReturnType == AdvancedQueryReturnTypeEnum.removeEmptyDataBlocks.rawValue
            
            plugin?.advancedQuery(variable: variable,
                                  startDate: Date(startDate),
                                  endDate: Date(endDate),
                                  timeUnit: timeUnit,
                                  operationType: operationType,
                                  mostRecent: false,
                                  onlyFilledBlocks: onlyFilledBlocks,
                                  timeUnitLength: timeUnitLength) { success, result, error in
                
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
