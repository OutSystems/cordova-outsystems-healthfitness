import Foundation

class HealthFitnessPlugin {
    
    let healthKitManager = HealthKitManager()
    
    func writeData(variable:String,
                   value:Double,
                   completion: @escaping (Bool, NSError?) -> Void) {
        
        healthKitManager.writeData(variable: variable, value: value) { (inner: CompletionHandler) -> Void in
            do {
                _ = try inner()
                completion(true, nil)
            } catch let error {
                completion(false, error as NSError)
            }
        }
    }
    
    func getLastRecord(variable:String,
                       mostRecent:Bool,
                       timeUnitLength:Int,
                       completion: @escaping (Bool, String?, NSError?) -> Void) {
        
        var finalResult: String?
        healthKitManager.advancedQuery(variable: variable,
                                       startDate: Date.distantPast,
                                       endDate: Date(),
                                       timeUnit: "",
                                       operationType: "",
                                       mostRecent: true,
                                       timeUnitLength: timeUnitLength) { result, error in

            if let error = error {
                completion(false,nil,error)
            } else if ((result != nil) && error == nil) {
                finalResult = result?.encode(object:result)
                completion(true,finalResult,nil)
            }

        }

    }

    func requestPermissions(customPermissions:String,
                            allVariables:String,
                            fitnessVariables:String,
                            healthVariables:String,
                            profileVariables:String,
                            summaryVariables:String,
                            completion: @escaping (Bool, NSError?) -> Void) {
        
        healthKitManager.authorizeHealthKit(customPermissions: customPermissions,
                                            allVariables:allVariables,
                                            fitnessVariables:fitnessVariables,
                                            healthVariables:healthVariables,
                                            profileVariables:profileVariables,
                                            summaryVariables:summaryVariables)
        { (authorized, error) in
            
            if let error = error {
                completion(false,error)
            } else if (authorized && error == nil) {
                completion(authorized,error)
            }
        
        }
         
    }
    
    func advancedQuery( variable: String,
                        startDate: Date,
                        endDate: Date,
                        timeUnit: String,
                        operationType: String,
                        mostRecent:Bool,
                        timeUnitLength: Int,
                        completion: @escaping(Bool, String?, NSError?) -> Void) {
        var finalResult: String?
        
        healthKitManager.advancedQuery(variable: variable,
                                       startDate: startDate,
                                       endDate: endDate,
                                       timeUnit: timeUnit,
                                       operationType: operationType,
                                       mostRecent: mostRecent,
                                       timeUnitLength: timeUnitLength) { result, error in
            
            if let error = error {
                completion(false,nil,error)
            } else if ((result != nil) && error == nil) {
                finalResult = result?.encode(object:result)
                completion(true,finalResult,nil)
            }
            
        }
    }
    
}
