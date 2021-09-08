
import Foundation

class HealthFitnessPlugin {

    func getData() -> String {
        return "Test String as result"
    }
    
    func requestPermissions(completion: @escaping (Bool, Error?) -> Void) {
        
        let healthKitManager = HealthKitManager()
    
        healthKitManager.authorizeHealthKit { (authorized, error) in
            
            guard authorized else {
                if let error = error {
                    completion(false,error)
                } else {
                    completion(true,nil)
                }
                
                return
            }
            
            print("HealthKit Successfully Authorized.")
        }
        
    }
    
    
}
