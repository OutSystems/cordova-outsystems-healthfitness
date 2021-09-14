import HealthKit

class HealthKitManager {
    
    var healthKitTypesToRead = Set<HKObjectType>()
    var healthKitTypesToWrite = Set<HKSampleType>()
    var HKTypes = HealthKitTypes()
        
    func isValidVariable(dict:[String: Any], variable:String) -> Bool {
        let filtered = dict.filter { $0.key == variable }
        return !filtered.isEmpty
    }
    
    func parseCustomPermissons(customPermissions:String) -> Bool {
        if let permissions = customPermissions.decode(string: customPermissions) as PermissionsArray?{
            for element in permissions {
                let variable = element.variable
                
                let existVariableToRead = isValidVariable(dict: HKTypes.allVariablesDictToRead, variable: variable)
                let existVariableToWrite = isValidVariable(dict: HKTypes.allVariablesDictToWrite, variable: variable)
                
                if (!variable.isEmpty) {
                    if (element.accessType == "WRITE" && existVariableToWrite) {
                        healthKitTypesToWrite.insert(HKTypes.allVariablesDictToWrite[variable]!)
                    }else if (element.accessType == "READWRITE") && existVariableToRead && existVariableToWrite {
                        healthKitTypesToRead.insert(HKTypes.allVariablesDictToRead[variable]!)
                        healthKitTypesToWrite.insert(HKTypes.allVariablesDictToWrite[variable]!)
                    } else if (existVariableToRead) {
                        healthKitTypesToRead.insert(HKTypes.allVariablesDictToRead[variable]!)
                    } else {
                        return false
                    }
                    
                } else {
                    return false
                }
            }
        }
        
        return true
    }
    
    func processVariables(dictToRead:[String: HKObjectType],
                        dictToWrite:[String: HKSampleType],
                        groupPermissions:GroupPermissions) {
        
        if (groupPermissions.accessType == "WRITE") {
            for item in dictToWrite { healthKitTypesToWrite.insert(item.value) }
        } else if (groupPermissions.accessType == "READWRITE") {
            for item in dictToRead { healthKitTypesToRead.insert(item.value) }
            for item in dictToWrite { healthKitTypesToWrite.insert(item.value) }
        } else {
            for item in dictToRead { healthKitTypesToRead.insert(item.value) }
        }
    }
    
    func authorizeHealthKit(customPermissions:String,
                            allVariables:String,
                            fitnessVariables:String,
                            healthVariables:String,
                            profileVariables:String,
                            summaryVariables:String,
                            completion: @escaping (Bool, NSError?) -> Void) {
        
        var isAuthorizationValid = true
        
        if let error = self.isHealthDataAvailable() {
            completion(false, error)
        }
        
        let all = allVariables.decode(string: allVariables) as GroupPermissions
        if all.isActive {
            self.processVariables(dictToRead: HKTypes.allVariablesDictToRead,
                                  dictToWrite: HKTypes.allVariablesDictToWrite,
                                  groupPermissions: all)
        }
        
        let fitness = fitnessVariables.decode(string: fitnessVariables) as GroupPermissions
        if fitness.isActive {
            self.processVariables(dictToRead: HKTypes.fitnessVariablesDictToRead,
                                  dictToWrite: HKTypes.fitnessVariablesDictToWrite,
                                  groupPermissions: fitness)
        }
        
        let health = healthVariables.decode(string: healthVariables) as GroupPermissions
        if health.isActive {
            self.processVariables(dictToRead: HKTypes.healthVariablesDictToRead,
                                  dictToWrite: HKTypes.healthVariablesDictToWrite,
                                  groupPermissions: health)
        }
        
        let profile = profileVariables.decode(string: profileVariables) as GroupPermissions
        if profile.isActive {
            self.processVariables(dictToRead: HKTypes.profileVariablesDictToRead,
                                  dictToWrite: HKTypes.profileVariablesDictToWrite,
                                  groupPermissions: profile)
        }
        
        let permissonsOK = self.parseCustomPermissons(customPermissions: customPermissions)
        if !permissonsOK {
            isAuthorizationValid = false
            completion(false, HealthKitErrors.dataTypeNotAvailable as NSError)
        }
        
        if (isAuthorizationValid) {
            HKHealthStore().requestAuthorization(toShare: healthKitTypesToWrite,
                                                 read: healthKitTypesToRead) { (success, error) in
                
                if (error != nil) {
                    return completion(false, HealthKitErrors.notAuthorizedByUser as NSError)
                }
                
                if success {
                    completion(success,error as NSError?)
                }
                
            }
        }
        
    }
    
    func isHealthDataAvailable() -> NSError? {
        
        guard HKHealthStore.isHealthDataAvailable() else {
            return HealthKitErrors.notAvailableOnDevice as NSError
        }
        return nil
    }
    
    func writeData(variable: String,
                   value: String,
                   completion: @escaping (Bool, NSError?) -> Void) {
        
        
        if let error = self.isHealthDataAvailable() {
            completion(false, error)
        }
                
        guard let type = HKTypes.profileVariablesQuantityDictToWrite[variable] else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(false, error as NSError)
            return
        }
        
        guard let unit = HKTypes.profileVariablesUnitDictToWrite[variable] else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(false, error as NSError)
            return
        }
        
        if let val = Double(value) {
            let variableQuantity = HKQuantity(unit: unit, doubleValue: val)
            
            let countSample = HKQuantitySample(type: type,
                                                quantity: variableQuantity,
                                                   start: Date(),
                                                   end: Date())
            
            HKHealthStore().save(countSample) { (success, error) in
                
                if let error = error {
                    switch (error as NSError).code {
                    case 1:
                        completion(false, HealthKitErrors.notAvailableOnDevice as NSError)
                    case 5:
                        completion(false, HealthKitErrors.notAuthorizedByUser as NSError)
                    default:
                        completion(false, error as NSError)
                    }
                    
                } else {
                    completion(true, nil)
                }
            }
        }
       
    }

    
}

extension String {
    
    func decode<T: Decodable>(string:String) -> T {
        let data: Data? = string.data(using: .utf8)
        return try! JSONDecoder().decode(T.self, from: data!)
    }

}
