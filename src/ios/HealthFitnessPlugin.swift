
import Foundation

class HealthFitnessPlugin {

    func getData() -> String {
        let healthKitManager = HealthKitManager()
        return healthKitManager.getData()
    }
    
    func requestPermissions(customPermissions:String, completion: @escaping (Bool, Error?) -> Void) {
        
        let healthKitManager = HealthKitManager()
        healthKitManager.authorizeHealthKit(customPermissions: customPermissions) { (authorized, error) in
            
            guard let error = error else {
                return completion(false,error)
            }
            
            if authorized {
                completion(authorized,error)
            }
        
        }
        
    }
    
}
