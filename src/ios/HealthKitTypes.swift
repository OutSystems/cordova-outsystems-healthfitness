
import Foundation
import HealthKit

// MARK: - HealthTypeEnum
enum HealthTypeEnum: String
{
    case stepCount = "STEPS",
         heartRate = "HEART_RATE",
         bodyMass = "WEIGHT",
         height = "HEIGHT",
         bloodGlucose = "BLOOD_PRESSURE_GLUCOSE",
         blood = "BLOOD_PRESSURE",
         sleepAnalysis = "SLEEP",
         oxygenSaturation = "OXYGEN_SATURATION",
         activeEnergyBurned = "CALORIES_BURNED",
         bodyFatPercentage = "BODY_FAT_PERCENTAGE",
         basalEnergyBurned = "BASAL_METABOLIC_RATE",
         bodyTemperature = "BODY_TEMPERATURE",
         dietaryWater = "HYDRATION",
         pushCount = "PUSH_COUNT"
}

class HealthKitTypes {
    
    // MARK: - All Variables
    lazy var allVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.stepCount.rawValue:HKObjectType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.heartRate.rawValue:HKObjectType.quantityType(forIdentifier: .heartRate)!,
         HealthTypeEnum.bodyMass.rawValue:HKObjectType.quantityType(forIdentifier: .bodyMass)!,
         HealthTypeEnum.bloodGlucose.rawValue:HKObjectType.quantityType(forIdentifier: .bloodGlucose)!,
         HealthTypeEnum.pushCount.rawValue:HKObjectType.quantityType(forIdentifier: .pushCount)!,
         HealthTypeEnum.bodyFatPercentage.rawValue:HKObjectType.quantityType(forIdentifier: .bodyFatPercentage)!,
         HealthTypeEnum.basalEnergyBurned.rawValue:HKObjectType.quantityType(forIdentifier: .basalEnergyBurned)!,
         HealthTypeEnum.bodyTemperature.rawValue:HKObjectType.quantityType(forIdentifier: .bodyTemperature)!,
         HealthTypeEnum.dietaryWater.rawValue:HKObjectType.quantityType(forIdentifier: .dietaryWater)!,
         HealthTypeEnum.oxygenSaturation.rawValue:HKObjectType.quantityType(forIdentifier: .oxygenSaturation)!,
         HealthTypeEnum.sleepAnalysis.rawValue:HKObjectType.categoryType(forIdentifier: .sleepAnalysis)!,
         HealthTypeEnum.activeEnergyBurned.rawValue:HKObjectType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!,
         HealthTypeEnum.height.rawValue:HKObjectType.quantityType(forIdentifier: .height)!]
    
    lazy var allVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.heartRate.rawValue:HKSampleType.quantityType(forIdentifier: .heartRate)!,
         HealthTypeEnum.bodyMass.rawValue:HKSampleType.quantityType(forIdentifier: .bodyMass)!,
         HealthTypeEnum.bloodGlucose.rawValue:HKSampleType.quantityType(forIdentifier: .bloodGlucose)!,
         HealthTypeEnum.pushCount.rawValue:HKSampleType.quantityType(forIdentifier: .pushCount)!,
         HealthTypeEnum.bodyTemperature.rawValue:HKSampleType.quantityType(forIdentifier: .bodyTemperature)!,
         HealthTypeEnum.dietaryWater.rawValue:HKSampleType.quantityType(forIdentifier: .dietaryWater)!,
         HealthTypeEnum.oxygenSaturation.rawValue:HKObjectType.quantityType(forIdentifier: .oxygenSaturation)!,
         HealthTypeEnum.sleepAnalysis.rawValue:HKSampleType.categoryType(forIdentifier: .sleepAnalysis)!,
         HealthTypeEnum.bodyFatPercentage.rawValue:HKSampleType.quantityType(forIdentifier: .bodyFatPercentage)!,
         HealthTypeEnum.basalEnergyBurned.rawValue:HKSampleType.quantityType(forIdentifier: .basalEnergyBurned)!,
         HealthTypeEnum.activeEnergyBurned.rawValue:HKSampleType.quantityType(forIdentifier: .activeEnergyBurned)!,
         HealthTypeEnum.height.rawValue:HKSampleType.quantityType(forIdentifier: .height)!]

    
    let activeEnergyBurned = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!,
                                                    sampleType: HKSampleType.quantityType(forIdentifier: .activeEnergyBurned)!,
                                                    objectType: HKObjectType.quantityType(forIdentifier: .activeEnergyBurned)!,
                                                    correlationType: nil,
                                                    unit: HKUnit.count())
    
    let heartRate = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.heartRate)!,
                                           sampleType: HKSampleType.quantityType(forIdentifier: .heartRate)!,
                                           objectType: HKObjectType.quantityType(forIdentifier: .heartRate)!,
                                           correlationType: nil,
                                           unit: HKUnit(from: "count/min"))
    
    let stepCount = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.stepCount)!,
                                           sampleType: HKSampleType.quantityType(forIdentifier: .stepCount)!,
                                           objectType: HKObjectType.quantityType(forIdentifier: .stepCount)!,
                                           correlationType: nil,
                                           unit: HKUnit.count())
    
    let bloodPressureSystolic = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.bloodPressureSystolic)!,
                                                       sampleType: HKSampleType.quantityType(forIdentifier: .bloodPressureSystolic)!,
                                                       objectType: HKObjectType.quantityType(forIdentifier: .bloodPressureSystolic)!,
                                                       correlationType: HKCorrelationType.correlationType(forIdentifier: .bloodPressure)!,
                                                       unit: HKUnit.count())
    
    let bloodPressureDiastolic = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.bloodPressureDiastolic)!,
                                                        sampleType: HKSampleType.quantityType(forIdentifier: .bloodPressureDiastolic)!,
                                                        objectType: HKObjectType.quantityType(forIdentifier: .bloodPressureDiastolic)!,
                                                        correlationType: HKCorrelationType.correlationType(forIdentifier: .bloodPressure)!,
                                                        unit: HKUnit.count())
    
    let bloodGlucose = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier: .bloodGlucose)!,
                                                       sampleType: HKSampleType.quantityType(forIdentifier: .bloodGlucose)!,
                                                       objectType: HKObjectType.quantityType(forIdentifier: .bloodGlucose)!,
                                                       correlationType: nil,
                                                       unit: HKUnit.count())
    
    
    
    lazy var allVariablesQuantityDictToQuery: [String: [HealthKitVariable]] = [
        HealthTypeEnum.blood.rawValue:[bloodPressureSystolic, bloodPressureDiastolic],
        HealthTypeEnum.stepCount.rawValue:[stepCount],
        HealthTypeEnum.heartRate.rawValue:[heartRate],
        HealthTypeEnum.bloodGlucose.rawValue:[bloodGlucose]
    ]
    
    lazy var allVariablesUnitDictToWrite: [String: HKUnit] =
        [HealthTypeEnum.bodyMass.rawValue:HKUnit.gramUnit(with: .kilo),
         HealthTypeEnum.stepCount.rawValue:HKUnit.count()]
    
    // MARK: - Profile Variables
    lazy var profileVariablesDictToRead: [String: HKObjectType] =
        [HealthTypeEnum.bodyMass.rawValue:HKObjectType.quantityType(forIdentifier: .bodyMass)!,
         HealthTypeEnum.height.rawValue:HKObjectType.quantityType(forIdentifier: .height)!]
    
    lazy var profileVariablesDictToWrite: [String: HKSampleType] =
        [HealthTypeEnum.stepCount.rawValue:HKSampleType.quantityType(forIdentifier: .stepCount)!,
         HealthTypeEnum.bodyFatPercentage.rawValue:HKSampleType.quantityType(forIdentifier: .bodyFatPercentage)!,
         HealthTypeEnum.basalEnergyBurned.rawValue:HKSampleType.quantityType(forIdentifier: .basalEnergyBurned)!,
         HealthTypeEnum.height.rawValue:HKSampleType.quantityType(forIdentifier: .height)!]
    
    lazy var profileVariablesQuantityDictToWrite: [String: HKQuantityType] =
        [HealthTypeEnum.bodyMass.rawValue:HKQuantityType.quantityType(forIdentifier: .bodyMass)!,
         HealthTypeEnum.bodyFatPercentage.rawValue:HKSampleType.quantityType(forIdentifier: .bodyFatPercentage)!,
         HealthTypeEnum.basalEnergyBurned.rawValue:HKSampleType.quantityType(forIdentifier: .basalEnergyBurned)!,
         HealthTypeEnum.height.rawValue:HKQuantityType.quantityType(forIdentifier: .height)!]
    
    lazy var profileVariablesUnitDictToWrite: [String: HKUnit] =
        [HealthTypeEnum.bodyMass.rawValue:HKUnit.gramUnit(with: .kilo),
         HealthTypeEnum.bodyFatPercentage.rawValue:HKUnit.percent(),
         HealthTypeEnum.basalEnergyBurned.rawValue:HKUnit.percent(),
         HealthTypeEnum.height.rawValue:HKUnit.inch()]
    
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

// MARK: - QueryParameters
class QueryParameters: Codable {
    let variable: String?
    let startDate, endDate: String?
    let timeUnit, operationType: String?

    enum CodingKeys: String, CodingKey {
        case variable = "Variable"
        case startDate = "StartDate"
        case endDate = "EndDate"
        case timeUnit = "TimeUnit"
        case operationType = "OperationType"
    }

    init(variable: String?, startDate: String?, endDate: String?, timeUnit: String?, operationType: String?) {
        self.variable = variable
        self.startDate = startDate
        self.endDate = endDate
        self.timeUnit = timeUnit
        self.operationType = operationType
    }
    
}

struct HealthKitVariable {
    var quantityType: HKQuantityType
    var sampleType: HKSampleType
    var objectType: HKObjectType
    var correlationType: HKCorrelationType?
    var unit: HKUnit
    var optionsAllowed: HKStatisticsOptions?
}


enum TimeUnit: String {
    case milliseconds = "MILLISECONDS",
         seconds = "SECONDS",
         minute = "MINUTE",
         hour = "HOUR",
         day = "DAY",
         week = "WEEK",
         month = "MONTH",
         year = "YEAR"
}


enum OperationType: String {
    case sum = "SUM",
         average = "AVERAGE",
         min = "MIN",
         max = "MAX"
}

struct AdvancedQueryResponseBlock: Codable {
    var block: Int?
    var startDate : Int?
    var endDate : Int?
    var values : [Float]?
}

struct AdvancedQueryResponse: Codable {
    var results : [AdvancedQueryResponseBlock]?
}
