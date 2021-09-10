
import Foundation

class HealthFitnessPlugin {

    func getData() -> String {
        let healthKitManager = HealthKitManager()
        return healthKitManager.getData()
    }
    
    func requestPermissions(customPermissions:String,
                            allVariables:String,
                            fitnessVariables:String,
                            healthVariables:String,
                            profileVariables:String,
                            summaryVariables:String,
                            completion: @escaping (Bool, HealthKitAuthorizationErrors?) -> Void) {
        
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
