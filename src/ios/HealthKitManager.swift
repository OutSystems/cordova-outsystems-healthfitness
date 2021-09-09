import HealthKit

class HealthKitManager {
    
    lazy var allVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.stepCount.rawValue:HKObjectType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.heartRate.rawValue:HKObjectType.quantityType(forIdentifier: .heartRate)!,
         HealthTypeEnum.activeEnergyBurned.rawValue:HKObjectType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!,
         HealthTypeEnum.height.rawValue:HKObjectType.quantityType(forIdentifier: .height)!]
    
    lazy var allVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.heartRate.rawValue:HKSampleType.quantityType(forIdentifier: .heartRate)!,
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

    private enum HealthKitAuthorizationError: Error {
        case notAvailableOnDevice
        case dataTypeNotAvailable
        case notAuthorizedByUser
    }

    func getData() -> String {
        return "Test String as result"
    }
    
    func parseCustomPermissons(customPermissions:String){
        let data: Data? = customPermissions.data(using: .utf8)
        if let permissions = try? JSONDecoder().decode(PermissionsArray.self, from: data!) {
            
            
            for element in permissions {
                let variable = element.variable
                if (!variable.isEmpty) {
                    if (element.accessType == "WRITE") {
                        healthKitTypesToWrite.insert(allVariablesDictToWrite[variable]!)
                    }else if (element.accessType == "READWRITE") {
                        healthKitTypesToRead.insert(allVariablesDictToRead[variable]!)
                        healthKitTypesToWrite.insert(allVariablesDictToWrite[variable]!)
                    } else {
                        healthKitTypesToRead.insert(allVariablesDictToRead[variable]!)
                    }
                }

            }
        }
        
    }
    
    func parseAllVariablesPermissons(allVariables:String){
        let data: Data? = allVariables.data(using: .utf8)
        if let groupPermissions = try? JSONDecoder().decode(GroupPermissions.self, from: data!) {
            if (groupPermissions.isActive) {
                
                if (groupPermissions.accessType == "WRITE") {
                    for item in allVariablesDictToWrite {
                        healthKitTypesToWrite.insert(item.value)
                    }
                    
                } else if (groupPermissions.accessType == "READWRITE") {
                    
                    for item in allVariablesDictToRead {
                        healthKitTypesToRead.insert(item.value)
                    }
                    for item in allVariablesDictToWrite {
                        healthKitTypesToWrite.insert(item.value)
                    }
                } else {
                    for item in allVariablesDictToRead {
                        healthKitTypesToRead.insert(item.value)
                    }
                }
                    
            }
            
        }
        
    }
    
    func parseFitnessVariablesPermissons(fitnessVariables:String){
        let data: Data? = fitnessVariables.data(using: .utf8)
        if let groupPermissions = try? JSONDecoder().decode(GroupPermissions.self, from: data!) {
            if (groupPermissions.isActive) {
                if (groupPermissions.accessType == "WRITE") {
                    
                    for item in allVariablesDictToWrite {
                        healthKitTypesToWrite.insert(item.value)
                    }
                    
                } else if (groupPermissions.accessType == "READWRITE") {
                    
                    for item in allVariablesDictToRead {
                        healthKitTypesToRead.insert(item.value)
                    }
                    for item in allVariablesDictToWrite {
                        healthKitTypesToWrite.insert(item.value)
                    }
                } else {
                    for item in allVariablesDictToRead {
                        healthKitTypesToRead.insert(item.value)
                    }
                }
            }
        }
    }
    
    func parseHealthVariablesPermissons(healthVariables:String){
        let data: Data? = healthVariables.data(using: .utf8)
        if let groupPermissions = try? JSONDecoder().decode(GroupPermissions.self, from: data!) {
            if (groupPermissions.isActive) {
                if (groupPermissions.accessType == "WRITE") {
                    
                    for item in allVariablesDictToWrite {
                        healthKitTypesToWrite.insert(item.value)
                    }
                    
                } else if (groupPermissions.accessType == "READWRITE") {
                    
                    for item in allVariablesDictToRead {
                        healthKitTypesToRead.insert(item.value)
                    }
                    for item in allVariablesDictToWrite {
                        healthKitTypesToWrite.insert(item.value)
                    }
                } else {
                    for item in allVariablesDictToRead {
                        healthKitTypesToRead.insert(item.value)
                    }
                }
            }
        }
    }
    
    func parseProfileVariablesPermissons(profileVariables:String){
        let data: Data? = profileVariables.data(using: .utf8)
        if let groupPermissions = try? JSONDecoder().decode(GroupPermissions.self, from: data!) {
            if (groupPermissions.isActive) {
                if (groupPermissions.accessType == "WRITE") {
                    
                    for item in allVariablesDictToWrite {
                        healthKitTypesToWrite.insert(item.value)
                    }
                    
                } else if (groupPermissions.accessType == "READWRITE") {
                    
                    for item in allVariablesDictToRead {
                        healthKitTypesToRead.insert(item.value)
                    }
                    for item in allVariablesDictToWrite {
                        healthKitTypesToWrite.insert(item.value)
                    }
                } else {
                    for item in allVariablesDictToRead {
                        healthKitTypesToRead.insert(item.value)
                    }
                }
            }
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
          completion(false, HealthKitAuthorizationError.notAvailableOnDevice)
          return
        }
        
        parseCustomPermissons(customPermissions: customPermissions)
        parseAllVariablesPermissons(allVariables: allVariables)
        parseFitnessVariablesPermissons(fitnessVariables: fitnessVariables)
        parseHealthVariablesPermissons(healthVariables: healthVariables)
        parseProfileVariablesPermissons(profileVariables: profileVariables)

        HKHealthStore().requestAuthorization(toShare: healthKitTypesToWrite,
                                             read: healthKitTypesToRead) { (success, error) in
            
            guard let error = error else {
                return completion(false, HealthKitAuthorizationError.notAuthorizedByUser)
            }
            
            if success {
                completion(success,error)
            }
            
        }
        
    }

}

