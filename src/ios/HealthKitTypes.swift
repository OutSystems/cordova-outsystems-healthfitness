
import Foundation
import HealthKit

// MARK: - HealthTypeEnum
enum HealthTypeEnum: String
{
    case stepCount = "STEPS",
         heartRate = "HEART_RATE",
         weight = "WEIGHT",
         height = "HEIGHT",
         bloodGlucose = "BLOOD_GLUCOSE",
         bloodPressure = "BLOOD_PRESSURE",
         sleepAnalysis = "SLEEP",
         oxygenSaturation = "OXYGEN_SATURATION",
         activeEnergyBurned = "CALORIES_BURNED",
         bodyFatPercentage = "BODY_FAT_PERCENTAGE",
         basalEnergyBurned = "BASAL_METABOLIC_RATE",
         bodyTemperature = "BODY_TEMPERATURE",
         dietaryWater = "HYDRATION",
         dietaryEnergyConsumed = "NUTRITION",
         pushCount = "PUSH_COUNT"
}

class HealthKitTypes {

    let stepCount = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.stepCount)!,
                                           sampleType: HKSampleType.quantityType(forIdentifier: .stepCount)!,
                                           objectType: HKObjectType.quantityType(forIdentifier: .stepCount)!,
                                           correlationType: nil,
                                           unit: HKUnit.count(),
                                           optionsAllowed: [.cumulativeSum,.mostRecent])

    let heartRate = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.heartRate)!,
                                           sampleType: HKSampleType.quantityType(forIdentifier: .heartRate)!,
                                           objectType: HKObjectType.quantityType(forIdentifier: .heartRate)!,
                                           correlationType: nil,
                                           unit: HKUnit(from: "count/min"),
                                           optionsAllowed: [.discreteAverage,.mostRecent,.discreteMax,.discreteMin])
    
    let bodyMass = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.bodyMass)!,
                                                    sampleType: HKSampleType.quantityType(forIdentifier: .bodyMass)!,
                                                    objectType: HKObjectType.quantityType(forIdentifier: .bodyMass)!,
                                                    correlationType: nil,
                                                    unit: HKUnit.gramUnit(with: .kilo),
                                                    optionsAllowed: [.discreteAverage,.mostRecent,.discreteMax,.discreteMin])
    
    let height = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.height)!,
                                                    sampleType: HKSampleType.quantityType(forIdentifier: .height)!,
                                                    objectType: HKObjectType.quantityType(forIdentifier: .height)!,
                                                    correlationType: nil,
                                                    unit: HKUnit.inch(),
                                                    optionsAllowed: [.discreteAverage,.mostRecent,.discreteMax,.discreteMin])
    
    let bloodGlucose = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier: .bloodGlucose)!,
                                                       sampleType: HKSampleType.quantityType(forIdentifier: .bloodGlucose)!,
                                                       objectType: HKObjectType.quantityType(forIdentifier: .bloodGlucose)!,
                                                       correlationType: nil,
                                                       unit: HKUnit(from: "mg/dL"),
                                                       optionsAllowed: [.discreteAverage,.mostRecent,.discreteMax,.discreteMin])
    
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
    
    let sleepAnalysis = HealthKitVariable.init(quantityType: nil, // TODO Verify
                                               sampleType: HKSampleType.categoryType(forIdentifier: .sleepAnalysis)!,
                                               objectType: HKObjectType.categoryType(forIdentifier: .sleepAnalysis)!,
                                               correlationType: nil,
                                               unit: HKUnit.count(),
                                               optionsAllowed: [.discreteAverage,.mostRecent,.discreteMax,.discreteMin])
    
    let oxygenSaturation = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.oxygenSaturation)!,
                                                        sampleType: HKSampleType.quantityType(forIdentifier: .oxygenSaturation)!,
                                                        objectType: HKObjectType.quantityType(forIdentifier: .oxygenSaturation)!,
                                                        correlationType: nil,
                                                        unit: HKUnit.percent(),
                                                        optionsAllowed: [.discreteAverage,.mostRecent,.discreteMax,.discreteMin])

    let activeEnergyBurned = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.activeEnergyBurned)!,
                                                    sampleType: HKSampleType.quantityType(forIdentifier: .activeEnergyBurned)!,
                                                    objectType: HKObjectType.quantityType(forIdentifier: .activeEnergyBurned)!,
                                                    correlationType: nil,
                                                    unit: HKUnit.kilocalorie(),
                                                    optionsAllowed: [.cumulativeSum,.mostRecent])
    
    let bodyFatPercentage = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.bodyFatPercentage)!,
                                                    sampleType: HKSampleType.quantityType(forIdentifier: .bodyFatPercentage)!,
                                                    objectType: HKObjectType.quantityType(forIdentifier: .bodyFatPercentage)!,
                                                    correlationType: nil,
                                                    unit: HKUnit.percent(),
                                                    optionsAllowed: [.discreteAverage,.mostRecent,.discreteMax,.discreteMin])
    
    let basalEnergyBurned = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.basalEnergyBurned)!,
                                                    sampleType: HKSampleType.quantityType(forIdentifier: .basalEnergyBurned)!,
                                                    objectType: HKObjectType.quantityType(forIdentifier: .basalEnergyBurned)!,
                                                    correlationType: nil,
                                                    unit: HKUnit.kilocalorie(),
                                                    optionsAllowed: [.cumulativeSum,.mostRecent])
    
    let bodyTemperature = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.bodyTemperature)!,
                                                    sampleType: HKSampleType.quantityType(forIdentifier: .bodyTemperature)!,
                                                    objectType: HKObjectType.quantityType(forIdentifier: .bodyTemperature)!,
                                                    correlationType: nil,
                                                    unit: HKUnit.degreeCelsius(),
                                                    optionsAllowed: [.discreteAverage,.mostRecent,.discreteMax,.discreteMin])
    
    let dietaryWater = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.dietaryWater)!,
                                              sampleType: HKSampleType.quantityType(forIdentifier: .dietaryWater)!,
                                              objectType: HKObjectType.quantityType(forIdentifier: .dietaryWater)!,
                                              correlationType: nil,
                                              unit: HKUnit.liter(),
                                              optionsAllowed: [.cumulativeSum,.mostRecent])
    
    let pushCount = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.pushCount)!,
                                              sampleType: HKSampleType.quantityType(forIdentifier: .pushCount)!,
                                              objectType: HKObjectType.quantityType(forIdentifier: .pushCount)!,
                                              correlationType: nil,
                                              unit: HKUnit.count(),
                                              optionsAllowed: [.cumulativeSum,.mostRecent])
    
    let dietaryEnergyConsumed = HealthKitVariable.init(quantityType: HKQuantityType.quantityType(forIdentifier:HKQuantityTypeIdentifier.dietaryEnergyConsumed)!,
                                              sampleType: HKSampleType.quantityType(forIdentifier: .dietaryEnergyConsumed)!,
                                              objectType: HKObjectType.quantityType(forIdentifier: .dietaryEnergyConsumed)!,
                                              correlationType: nil,
                                              unit: HKUnit.kilocalorie(),
                                              optionsAllowed: [.cumulativeSum,.mostRecent])
    
    
    // MARK: - All Variables
    lazy var allVariablesDict: [String: [HealthKitVariable]] = [
        HealthTypeEnum.stepCount.rawValue:[stepCount],
        HealthTypeEnum.heartRate.rawValue:[heartRate],
        HealthTypeEnum.weight.rawValue:[bodyMass],
        HealthTypeEnum.height.rawValue:[height],
        HealthTypeEnum.bloodGlucose.rawValue:[bloodGlucose],
        HealthTypeEnum.bloodPressure.rawValue:[bloodPressureSystolic, bloodPressureDiastolic],
        HealthTypeEnum.sleepAnalysis.rawValue:[sleepAnalysis],
        HealthTypeEnum.oxygenSaturation.rawValue:[oxygenSaturation],
        HealthTypeEnum.activeEnergyBurned.rawValue:[activeEnergyBurned],
        HealthTypeEnum.basalEnergyBurned.rawValue:[basalEnergyBurned],
        HealthTypeEnum.bodyFatPercentage.rawValue:[bodyFatPercentage],
        HealthTypeEnum.bodyTemperature.rawValue:[bodyTemperature],
        HealthTypeEnum.dietaryWater.rawValue:[dietaryWater],
        HealthTypeEnum.dietaryEnergyConsumed.rawValue:[dietaryEnergyConsumed],
        HealthTypeEnum.pushCount.rawValue:[pushCount]
    ]
    
    // MARK: - Profile Variables
    lazy var profileVariablesDict: [String: [HealthKitVariable]] =
        [HealthTypeEnum.weight.rawValue:[bodyMass],
         HealthTypeEnum.bodyFatPercentage.rawValue:[bodyFatPercentage],
         HealthTypeEnum.basalEnergyBurned.rawValue:[basalEnergyBurned],
         HealthTypeEnum.height.rawValue:[height]]
    
    // MARK: - Fitness Variables
    lazy var fitnessVariablesDict: [String: [HealthKitVariable]] =
        [HealthTypeEnum.stepCount.rawValue:[stepCount],
         HealthTypeEnum.pushCount.rawValue:[pushCount],
         HealthTypeEnum.activeEnergyBurned.rawValue:[activeEnergyBurned]]

    // MARK: - Health Variables
    lazy var healthVariablesDict: [String: [HealthKitVariable]] =
        [HealthTypeEnum.sleepAnalysis.rawValue:[sleepAnalysis],
         HealthTypeEnum.bloodPressure.rawValue:[bloodPressureSystolic, bloodPressureDiastolic],
         HealthTypeEnum.bloodGlucose.rawValue:[bloodGlucose],
         HealthTypeEnum.oxygenSaturation.rawValue:[oxygenSaturation],
         HealthTypeEnum.bodyTemperature.rawValue:[bodyTemperature],
         HealthTypeEnum.dietaryEnergyConsumed.rawValue:[dietaryEnergyConsumed],
         HealthTypeEnum.dietaryWater.rawValue:[dietaryWater],
         HealthTypeEnum.heartRate.rawValue:[heartRate]]
        
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
    let timeUnitLength: Int?

    enum CodingKeys: String, CodingKey {
        case variable = "Variable"
        case startDate = "StartDate"
        case endDate = "EndDate"
        case timeUnit = "TimeUnit"
        case operationType = "OperationType"
        case timeUnitLength = "TimeUnitLength"
    }

    init(variable: String?, startDate: String?, endDate: String?, timeUnit: String?, operationType: String?, timeUnitLength:Int?) {
        self.variable = variable
        self.startDate = startDate
        self.endDate = endDate
        self.timeUnit = timeUnit
        self.operationType = operationType
        self.timeUnitLength = timeUnitLength
    }
    
}

struct HealthKitVariable {
    var quantityType: HKQuantityType?
    var sampleType: HKSampleType
    var objectType: HKObjectType
    var correlationType: HKCorrelationType?
    var unit: HKUnit
    var optionsAllowed: [HKStatisticsOptions]?
    //var defaultOption: HKStatisticsOptions?
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

enum AccessTypeEnum: String {
    case write = "WRITE",
         read = "READ",
         readWrite = "READWRITE"
}


struct AdvancedQueryResponseBlock: Encodable {
    var block: Int?
    var startDate : Int?
    var endDate : Int?
    var values : [Float]?
}

struct AdvancedQueryResponse: Encodable {
    var results : [AdvancedQueryResponseBlock]?
}
