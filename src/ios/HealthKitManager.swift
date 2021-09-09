

import HealthKit

class HealthKitManager {
    
    lazy var HKTypesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.stepCount.rawValue:HKObjectType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.heartRate.rawValue:HKObjectType.quantityType(forIdentifier: .heartRate)!,
         HealthTypeEnum.height.rawValue:HKObjectType.quantityType(forIdentifier: .height)!]
    
    
    lazy var HKTypesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.heartRate.rawValue:HKSampleType.quantityType(forIdentifier: .heartRate)!,
         HealthTypeEnum.height.rawValue:HKSampleType.quantityType(forIdentifier: .height)!]

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
    
    func authorizeHealthKit(customPermissions:String,
                            completion: @escaping (Bool, Error?) -> Void) {
        
        guard HKHealthStore.isHealthDataAvailable() else {
          completion(false, HealthKitAuthorizationError.notAvailableOnDevice)
          return
        }
        
        let data: Data? = customPermissions.data(using: .utf8)
        if let permissions = try? JSONDecoder().decode(PermissionsArray.self, from: data!) {
            for element in permissions {
                let variable = element.variable
                if (element.accessType == "WRITE") {
                    healthKitTypesToWrite.insert(HKTypesDictToWrite[variable]!)
                }else if (element.accessType == "READWRITE") {
                    healthKitTypesToRead.insert(HKTypesDictToRead[variable]!)
                    healthKitTypesToWrite.insert(HKTypesDictToWrite[variable]!)
                } else {
                    healthKitTypesToRead.insert(HKTypesDictToRead[variable]!)
                }
            }
        }
        
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

