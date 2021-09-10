import HealthKit

class HealthKitManager {
    
    lazy var allVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.stepCount.rawValue:HKObjectType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.heartRate.rawValue:HKObjectType.quantityType(forIdentifier: .heartRate)!,
         HealthTypeEnum.bodyMass.rawValue:HKObjectType.quantityType(forIdentifier: .bodyMass)!,
         HealthTypeEnum.activeEnergyBurned.rawValue:HKObjectType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!,
         HealthTypeEnum.height.rawValue:HKObjectType.quantityType(forIdentifier: .height)!]
    
    lazy var allVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.heartRate.rawValue:HKSampleType.quantityType(forIdentifier: .heartRate)!,
         HealthTypeEnum.bodyMass.rawValue:HKSampleType.quantityType(forIdentifier: .bodyMass)!,
         HealthTypeEnum.activeEnergyBurned.rawValue:HKSampleType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!,
         HealthTypeEnum.height.rawValue:HKSampleType.quantityType(forIdentifier: .height)!]
    
    lazy var profileVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.stepCount.rawValue:HKObjectType.quantityType(forIdentifier: .bodyMass)!,
         HealthTypeEnum.height.rawValue:HKObjectType.quantityType(forIdentifier: .height)!]
    
    lazy var profileVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.height.rawValue:HKSampleType.quantityType(forIdentifier: .height)!]
    
    lazy var fitnessVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.stepCount.rawValue:HKObjectType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.activeEnergyBurned.rawValue:HKObjectType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!]
    
    lazy var fitnessVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.activeEnergyBurned.rawValue:HKSampleType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!]
    
    lazy var healthVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.categoryType(forIdentifier: .sleepAnalysis)!,
         HealthTypeEnum.height.rawValue:HKObjectType.quantityType(forIdentifier: .height)!]
    
    lazy var healthVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.sleepAnalysis.rawValue:HKSampleType.categoryType(forIdentifier: .sleepAnalysis)!,
         HealthTypeEnum.height.rawValue:HKSampleType.quantityType(forIdentifier: .heartRate)!]
    

    var healthKitTypesToRead = Set<HKObjectType>()
    var healthKitTypesToWrite = Set<HKSampleType>()



    func getData() -> String {
        return "Test String as result"
    }
    
    func isValidField(dict:[String: Any], variable:String) -> Bool {
        let filtered = dict.filter { $0.key == variable }
        return !filtered.isEmpty
    }
    
    func parseCustomPermissons(customPermissions:String) -> Bool {
        if let permissions = customPermissions.decode(string: customPermissions) as PermissionsArray?{
            for element in permissions {
                let variable = element.variable
                
                let existVariableToRead = isValidField(dict: allVariablesDictToRead, variable: variable)
                let existVariableToWrite = isValidField(dict: allVariablesDictToWrite, variable: variable)
                
                if (!variable.isEmpty && existVariableToRead && existVariableToWrite) {
                    if (element.accessType == "WRITE") {
                        healthKitTypesToWrite.insert(allVariablesDictToWrite[variable]!)
                    }else if (element.accessType == "READWRITE") {
                        healthKitTypesToRead.insert(allVariablesDictToRead[variable]!)
                        healthKitTypesToWrite.insert(allVariablesDictToWrite[variable]!)
                    } else {
                        healthKitTypesToRead.insert(allVariablesDictToRead[variable]!)
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
                        groupPermissions:GroupPermissions)
    {
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
                            completion: @escaping (Bool, Error?) -> Void) {
        
        guard HKHealthStore.isHealthDataAvailable() else {
            completion(false, HealthKitAuthorizationErrors.notAvailableOnDevice as? Error)
          return
        }
        
        let all = allVariables.decode(string: allVariables) as GroupPermissions
        if all.isActive {
            self.processVariables(dictToRead: allVariablesDictToRead,
                                  dictToWrite: allVariablesDictToWrite,
                                  groupPermissions: all)
        }
        
        let fitness = fitnessVariables.decode(string: fitnessVariables) as GroupPermissions
        if fitness.isActive {
            self.processVariables(dictToRead: fitnessVariablesDictToRead,
                                  dictToWrite: fitnessVariablesDictToWrite,
                                  groupPermissions: fitness)
        }
        
        let health = healthVariables.decode(string: healthVariables) as GroupPermissions
        if health.isActive {
            self.processVariables(dictToRead: healthVariablesDictToRead,
                                  dictToWrite: healthVariablesDictToWrite,
                                  groupPermissions: health)
        }
        
        let profile = profileVariables.decode(string: profileVariables) as GroupPermissions
        if profile.isActive {
            self.processVariables(dictToRead: profileVariablesDictToRead,
                                  dictToWrite: profileVariablesDictToWrite,
                                  groupPermissions: profile)
        }
        
        let permissonsOK = self.parseCustomPermissons(customPermissions: customPermissions)
        if !permissonsOK {
            return completion(false, HealthKitAuthorizationErrors.dataTypeNotAvailable as? Error)
        }
        
        HKHealthStore().requestAuthorization(toShare: healthKitTypesToWrite,
                                             read: healthKitTypesToRead) { (success, error) in
            
            guard let error = error else {
                return completion(false, HealthKitAuthorizationErrors.notAuthorizedByUser as? Error)
            }
            
            if success {
                completion(success,error)
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
