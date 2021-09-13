
import Foundation
import HealthKit

class HealthKitTypes {
    
    // MARK: - All Variables
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
    
    // MARK: - Profile Variables
    lazy var profileVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.bodyMass.rawValue:HKObjectType.quantityType(forIdentifier: .bodyMass)!,
         HealthTypeEnum.height.rawValue:HKObjectType.quantityType(forIdentifier: .height)!]
    
    lazy var profileVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.height.rawValue:HKSampleType.quantityType(forIdentifier: .height)!]
    
    lazy var profileVariablesQuantityDictToWrite: [String: HKQuantityType] =
        [HealthTypeEnum.bodyMass.rawValue:HKQuantityType.quantityType(forIdentifier: .bodyMass)!,
         HealthTypeEnum.height.rawValue:HKQuantityType.quantityType(forIdentifier: .height)!]
    
    lazy var profileVariablesUnitDictToWrite: [String: HKUnit] =
        [HealthTypeEnum.bodyMass.rawValue:HKUnit.gramUnit(with: .kilo)]
    
    // MARK: - Fitness Variables
    lazy var fitnessVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.stepCount.rawValue:HKObjectType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.activeEnergyBurned.rawValue:HKObjectType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!]
    
    lazy var fitnessVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.activeEnergyBurned.rawValue:HKSampleType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!]

    // MARK: - Health Variables
    lazy var healthVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.sleepAnalysis.rawValue:HKSampleType.categoryType(forIdentifier: .sleepAnalysis)!,
         HealthTypeEnum.heartRate.rawValue:HKObjectType.quantityType(forIdentifier: .heartRate)!]
    
    lazy var healthVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.sleepAnalysis.rawValue:HKSampleType.categoryType(forIdentifier: .sleepAnalysis)!,
         HealthTypeEnum.heartRate.rawValue:HKSampleType.quantityType(forIdentifier: .heartRate)!]
        
}

// MARK: - HealthTypeEnum
enum HealthTypeEnum: String
{
    case stepCount = "STEPS",
         heartRate = "HEART_RATE",
         bodyMass = "WEIGHT",
         height = "HEIGTH",
         bloodGlucose = "BLOOD_PRESSURE_GLUCOSE",
         sleepAnalysis = "SLEEP",
         activeEnergyBurned = "CALORIES_BURNED"
}

// MARK: - CustomPermissions
class CustomPermissions: Codable {
    let variable,
        accessType: String

    enum CodingKeys: String, CodingKey {
        case variable = "Variable"
        case accessType = "AccessType"
    }

    init(variable: String, accessType: String) {
        self.variable = variable
        self.accessType = accessType
    }
}

typealias PermissionsArray = [CustomPermissions]

// MARK: - GroupPermissions
class GroupPermissions: Codable {
    let isActive: Bool
    let accessType: String
    
    enum CodingKeys: String, CodingKey {
        case isActive = "IsActive"
        case accessType = "AccessType"
    }

    init(isActive: Bool, accessType: String) {
        self.isActive = isActive
        self.accessType = accessType
    }
    
}
