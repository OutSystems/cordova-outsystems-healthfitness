
import Foundation

class HealthFitnessPlugin {
    
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

    func queryData(dataType: String, startDate: Date, endDate: Date, completion: @escaping(String?, Error?) -> Void) {
        let healthKitManager = HealthKitManager()
        var finalResult: String?
        var finalError: Error?
        
        //MARK - TODO: Acertar o completion handler
        healthKitManager.queryData(dataType: dataType, startDate: startDate, endDate: endDate) { result, error in
            
            if error != nil {
                finalError = error
                print(error!.localizedDescription)
            } else {
                //MARK - TODO: ver o conteúdo que preciso retornar e transformar o jsonEncoder em uma function
                let encoder = JSONEncoder()
                encoder.outputFormatting = .prettyPrinted
                let data = try! encoder.encode(result)
                print(String(data: data, encoding: .utf8)!)
                finalResult = String(data: data, encoding: .utf8)!
        
            }
            completion(finalResult,finalError)
        }
    }
    
}
