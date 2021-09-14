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
    
    @objc(queryData:)
    func queryData(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId

        if let startDateInput = command.arguments[0] as? String,
           let endDateInput = command.arguments[1] as? String,
           let dataType = command.arguments[2] as? String {

            let startDate = Date(startDateInput)
            let endDate = Date(endDateInput)

            plugin?.queryData(dataType: dataType, startDate: startDate, endDate: endDate) { result, error in
                
                if error != nil {
                    self.sendResult(result: nil, error: error?.localizedDescription, callBackID: self.callbackId)
                }
                else if result != nil {
                    self.sendResult(result: result, error: nil, callBackID: self.callbackId)
                }
                else {
                    //Should not happen, but a "catch all"
                    self.sendResult(result: nil, error: "An unknow error has occurred while trying to fetch HealthKit Data", callBackID: self.callbackId)
                }
            }
        }
    }
    
}

extension Date {
    init(_ dateString:String) {
        let dateStringFormatter = DateFormatter()
        dateStringFormatter.dateFormat = "dd-MM-yyyy"
        dateStringFormatter.locale = NSLocale(localeIdentifier: "en_US_POSIX") as Locale
        let date = dateStringFormatter.date(from: dateString)!
        self.init(timeInterval:0, since:date)
    }
}
