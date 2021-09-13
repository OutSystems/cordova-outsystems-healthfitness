
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
            }
        
        }
        
    }
    
    func requestPermissions(customPermissions:String,
                            allVariables:String,
                            fitnessVariables:String,
                            healthVariables:String,
                            profileVariables:String,
                            summaryVariables:String,
                            completion: @escaping (Bool, HealthKitErrors?) -> Void) {
        
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
    
}
