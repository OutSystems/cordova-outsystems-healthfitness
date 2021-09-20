
import Foundation

class HealthFitnessPlugin {

    func writeData(variable:String,
                   value:String,
                   completion: @escaping (Bool, NSError?) -> Void) {
        
        let healthKitManager = HealthKitManager()
        healthKitManager.writeData(variable: variable,
                                   value: value) { (success, error) in
            
            if let error = error {
                completion(false,error)
            } else {
                completion(true,nil)
            }
                
        }
        
    }
    
    func getLastRecord(variable:String,
                       completion: @escaping (Bool, Double?, NSError?) -> Void) {
        
        let healthKitManager = HealthKitManager()
        healthKitManager.getLastRecord(variable: variable) { (result, error) in
            
            if let error = error {
                completion(false, 0, error)
            } else {
                completion(true, result, nil)
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
        
        let healthKitManager = HealthKitManager()
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
    
    func getData(variable: String,
                 startDate: Date,
                 endDate: Date,
                 timeUnit: String,
                 operationType: String,
                 completion: @escaping(Bool, String?, NSError?) -> Void) {
        let healthKitManager = HealthKitManager()
        var finalResult: String?
        
        healthKitManager.getData(variable: variable,
                                 startDate: startDate,
                                 endDate: endDate,
                                 timeUnit: timeUnit,
                                 operationType: operationType) { result, error in
            
            if let error = error {
                completion(false,nil,error)
            } else if ((result != nil) && error == nil) {
                finalResult = result?.encode(object:result)
                completion(true,finalResult,nil)
            }
            
        }
    }
    
}
