import Foundation
import HealthKit

protocol HealthKitManagerProtocol {
    
    func checkAuthorizationStatus(for type: HKObjectType) -> HKAuthorizationStatus
    
    func writeData(sample: HKQuantitySample, completion: @escaping (_ inner: @escaping CompletionHandler) -> Void)
    
    func executeAdvancedQuery(quantityType: HKQuantityType,
                              options: HKStatisticsOptions,
                              anchorDate: Date,
                              interval: DateComponents,
                              newStartDate:Date,
                              completion: @escaping (Result<HKStatisticsCollection?, Error>) -> Void)
    
    func executeSimpleQuery(sample: HKSampleType,
                            predicate: NSPredicate,
                            limit:Int,
                            sortDescriptors: [NSSortDescriptor],
                            completion: @escaping (Result<[HKCorrelation]?, Error>) -> Void)
    
    func requestAuthorization(setToWrite: Set<HKSampleType>?,
                              setToRead: Set<HKObjectType>?,
                              completion: @escaping (Bool, NSError?) -> Void)
    
}

extension HKHealthStore: HealthKitManagerProtocol {
    
    func requestAuthorization(setToWrite: Set<HKSampleType>?,
                                     setToRead: Set<HKObjectType>?,
                                     completion: @escaping (Bool, NSError?) -> Void) {
        
        self.requestAuthorization(toShare: setToWrite,
                                     read: setToRead) { (success, error) in
            
            if success {
                completion(success,error as NSError?)
            } else {
                completion(false, HealthKitErrors.authorizationError as NSError)
            }
            
        }
        
    }
    
    func checkAuthorizationStatus(for type: HKObjectType) -> HKAuthorizationStatus {
        return self.authorizationStatus(for: type)
    }
    
    func executeAdvancedQuery(quantityType: HKQuantityType,
                              options: HKStatisticsOptions,
                              anchorDate: Date,
                              interval: DateComponents,
                              newStartDate: Date,
                              completion: @escaping (Result<HKStatisticsCollection?, Error>) -> Void) {

        let query = HKStatisticsCollectionQuery(quantityType: quantityType,
                                                quantitySamplePredicate: nil,
                                                options: options,
                                                anchorDate: anchorDate,
                                                intervalComponents: interval)
            
        query.initialResultsHandler = { _, results, error in
            guard let results = results else { return }

            if let error = error {
                completion(.failure(error))
            } else {
                completion(.success(results))
            }
        }
        
        self.execute(query)
    }
    
    func executeSimpleQuery(sample: HKSampleType,
                            predicate: NSPredicate,
                            limit: Int,
                            sortDescriptors: [NSSortDescriptor],
                            completion: @escaping (Result<[HKCorrelation]?, Error>) -> Void) {
        
        let sampleQuery = HKSampleQuery(sampleType: sample,
                                        predicate: predicate,
                                        limit: limit,
                                        sortDescriptors: sortDescriptors) { (sampleQuery, results, error ) in
            
            if let error = error {
                completion(.failure(error))
            } else {
                completion(.success(results as? [HKCorrelation]))
            }
        }
        self.execute(sampleQuery)
    }
    

    func writeData(sample: HKQuantitySample, completion: @escaping (@escaping CompletionHandler) -> Void) {
        
        self.save(sample) { (success, error) in
            
            if let error = error {
                completion ({ throw error })
            } else {
                completion ( { nil } )
            }
        }
    }
    
}

class HealthKitManager {
    
    var store: HealthKitManagerProtocol?
    var healthKitTypesToRead = Set<HKObjectType>()
    var healthKitTypesToWrite = Set<HKSampleType>()
    var HKTypes = HealthKitTypes()
    
    init(store:HealthKitManagerProtocol) {
        self.store = store
    }
    
    func parseCustomPermissons(customPermissions:String) -> Bool {
        if let permissions = customPermissions.decode(string: customPermissions) as PermissionsArray? {
            for element in permissions {
                let variable = element.variable
                
                if let type = HKTypes.allVariablesDict[variable] {
                    if type.count > 1 {
                        for item in type {
                            self.fillSets(accessType: element.accessType,
                                          sampleType: item.sampleType,
                                          objectType: item.objectType)
                        }
                        
                    } else {
                        guard let sampleType = type.first?.sampleType, let objectType = type.first?.objectType else { return false }
                        self.fillSets(accessType: element.accessType,
                                      sampleType: sampleType,
                                      objectType: objectType)
                        
                    }
        
                } else {
                    return false
                }
            
            }
        }
        
        return true
    }
    
    func fillSets(accessType:String,
                  sampleType: HKSampleType,
                  objectType: HKObjectType) {
        
        if (accessType == AccessTypeEnum.readWrite.rawValue) {
            healthKitTypesToWrite.insert(sampleType)
            healthKitTypesToRead.insert(objectType)
        } else if (accessType == AccessTypeEnum.write.rawValue) {
            healthKitTypesToWrite.insert(sampleType)
        } else {
            healthKitTypesToRead.insert(objectType)
        }
        
    }
    
    func fillPermissionSetWithVariables(dict: [String: [HealthKitVariable]],
                                        groupPermissions:GroupPermissions) {
                
        for (_, variables) in dict {
            if variables.count > 1 {
                for item in variables {
                    self.fillSets(accessType: groupPermissions.accessType,
                                  sampleType: item.sampleType,
                                  objectType: item.objectType)
                }
                
            } else {
                if let variable = variables.first {
                    self.fillSets(accessType: groupPermissions.accessType,
                                  sampleType: variable.sampleType,
                                  objectType: variable.objectType)
                }
                
            }
        }
    }
    
    func setPermissionsFor(variable: HealthKitVariable){
        let authStatus = self.store?.checkAuthorizationStatus(for: variable.objectType)
        if (authStatus == .sharingAuthorized) {
            healthKitTypesToWrite.insert(variable.sampleType)
        }
        
        let authStatusRead = self.store?.checkAuthorizationStatus(for: variable.objectType)
        if !(authStatusRead == .notDetermined) {
            healthKitTypesToRead.insert(variable.objectType)
        }
    }
    
    func fillSetsWithHistory(){
        for (_,item) in HKTypes.allVariablesDict.enumerated() {
            if item.value.count > 1 {
                for element in item.value {
                    setPermissionsFor(variable: element)
                }
            } else {
                if let variable = item.value.first {
                    setPermissionsFor(variable: variable)
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
                            completion: @escaping (Bool, NSError?) -> Void) {
        
        var isAuthorizationValid = true
        
        if let error = isHealthDataAvailable() {
            completion(false, error)
        }
        
        self.fillSetsWithHistory()
        
        if !allVariables.isEmpty {
            let all = allVariables.decode(string: allVariables) as GroupPermissions
            if all.isActive {
                self.fillPermissionSetWithVariables(dict: HKTypes.allVariablesDict,
                                      groupPermissions: all)
            }
        }

        if !fitnessVariables.isEmpty {
            let fitness = fitnessVariables.decode(string: fitnessVariables) as GroupPermissions
            if fitness.isActive {
                self.fillPermissionSetWithVariables(dict: HKTypes.fitnessVariablesDict,
                                                    groupPermissions: fitness)
            }
        }

        if !healthVariables.isEmpty {
            let health = healthVariables.decode(string: healthVariables) as GroupPermissions
            if health.isActive {
                self.fillPermissionSetWithVariables(dict: HKTypes.healthVariablesDict,
                                                    groupPermissions: health)
            }
        }
        
        if !profileVariables.isEmpty {
            let profile = profileVariables.decode(string: profileVariables) as GroupPermissions
            if profile.isActive {
                self.fillPermissionSetWithVariables(dict: HKTypes.profileVariablesDict,
                                                    groupPermissions: profile)
            }
        }

        if !customPermissions.isEmpty {
            let permissonsOK = self.parseCustomPermissons(customPermissions: customPermissions)
            if !permissonsOK {
                isAuthorizationValid = false
                completion(false, HealthKitErrors.variableNotAvailable as NSError)
            }
        }

        
        if (isAuthorizationValid) {

            self.store?.requestAuthorization(setToWrite:self.healthKitTypesToWrite,
                                             setToRead:self.healthKitTypesToRead) { [self] (success, error) in

                self.healthKitTypesToWrite.removeAll()
                self.healthKitTypesToRead.removeAll()
                
                if success {
                    completion(success,error as NSError?)
                } else {
                    return completion(false, error as NSError?)
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
    
    func getCalendarComponent(date: Date) -> DateComponents {
        let calendar = NSCalendar.current
        let calendarComponent = calendar.dateComponents([.second, .minute, .hour, .day, .weekOfYear, .month ,.year], from: date)
        return calendarComponent
    }
    
    func getStatisticOptions(operationType: String) -> HKStatisticsOptions {
        var HKOptions = HKStatisticsOptions()
        switch operationType {
            case OperationType.sum.rawValue:
                HKOptions = [.cumulativeSum]
            case OperationType.min.rawValue:
                HKOptions = [.discreteMin]
            case OperationType.max.rawValue:
                HKOptions = [.discreteMax]
            case OperationType.average.rawValue:
                HKOptions = [.discreteAverage]
            case OperationType.raw.rawValue:
                HKOptions = [.discreteAverage]
            default:
                HKOptions = []
        }
        return HKOptions
    }
    
    func getInterval(timeUnit:String, timeUnitLength:Int) -> DateComponents {
        var interval = DateComponents()
        switch timeUnit {
            case TimeUnit.milliseconds.rawValue:
                interval.second = timeUnitLength
            case TimeUnit.seconds.rawValue:
                interval.second = timeUnitLength
            case TimeUnit.minute.rawValue:
                interval.minute = timeUnitLength
            case TimeUnit.hour.rawValue:
                interval.hour = timeUnitLength
            case TimeUnit.day.rawValue:
                interval.day = timeUnitLength
            case TimeUnit.week.rawValue:
                interval.weekOfYear = timeUnitLength
            case TimeUnit.month.rawValue:
                interval.month = timeUnitLength
            case TimeUnit.year.rawValue:
                interval.year = timeUnitLength
            default:
                interval.day = timeUnitLength
        }
        return interval
    }
    
    func writeData(variable: String,
                   value: Double?,
                   completion: @escaping (_ inner: @escaping () throws -> HealthKitErrors?) -> Void) {
            
        if let error = self.isHealthDataAvailable() {
            completion ( { throw error } )
        }
        
        guard let type = self.HKTypes.allVariablesDict[variable] else {
            completion ({ throw HealthKitErrors.variableNotAvailable })
            return
        }
        
        guard let unit = type.first?.unit else {
            completion ({ throw HealthKitErrors.variableNotAvailable })
            return
        }
        
        guard let quantityType = type.first?.quantityType else {
            completion ({ throw HealthKitErrors.variableNotAvailable } )
            return
        }
        
        if let objectType = type.first?.objectType {
            let authStatus = self.store?.checkAuthorizationStatus(for: objectType)
            if authStatus == .sharingDenied {
                completion ( { throw HealthKitErrors.variableHasWriteDenied } )
                return
            }
        }

        if var val = value {
            
            if unit == HKUnit.percent() { val = (val/100) }
            let countSample = HKQuantitySample(type: quantityType,
                                           quantity: HKQuantity(unit: unit, doubleValue: val),
                                              start: Date(),
                                                end: Date())
            
            self.store?.writeData(sample: countSample) { (inner: () throws -> HealthKitErrors?) -> Void in
                
                do {
                    _ = try inner()
                    completion ( { nil } )
                } catch let error {
                    switch (error as NSError).code {
                    case 1:
                        completion ({ throw HealthKitErrors.notAvailableOnDevice })
                    case 5:
                        completion ({ throw HealthKitErrors.authorizationError })
                    default:
                        completion ({ throw error })
                    }
                }
                
            }
        }
    }

    func processAdvancedQueryResult(newStartDate:Date,
                                    endDate:Date,
                                    mostRecent:Bool,
                                    unit: HKUnit,
                                    result: HKStatisticsCollection) -> AdvancedQueryResponse {
    
        var block = 0
        var resultInfo = AdvancedQueryResponseBlock()
        var resultInfoArray = [AdvancedQueryResponseBlock]()
        var floatArray = [Double]()
        
        result.enumerateStatistics(from: newStartDate, to: endDate) { statistics, _ in
            
            if mostRecent {

                if let mostRecentQuantity = statistics.mostRecentQuantity() {
                    
                    resultInfo.block = block
                    resultInfo.startDate = Int(statistics.startDate.timeIntervalSince1970)
                    resultInfo.endDate = Int(statistics.endDate.timeIntervalSince1970)
                    resultInfo.values = []
                    
                    if unit == HKUnit.percent() {
                        floatArray.append(mostRecentQuantity.doubleValue(for: unit) * 100)
                    } else {
                        let roundedValue = round(mostRecentQuantity.doubleValue(for: unit) * 100) / 100.0
                        floatArray.append(roundedValue)
                    }
                    
                    resultInfo.values = floatArray
                    floatArray.removeAll()
                    
                    block+=1
                    resultInfoArray.append(resultInfo)
                }
                
            } else {
                
                resultInfo.block = block
                resultInfo.startDate = Int(statistics.startDate.timeIntervalSince1970)
                resultInfo.endDate = Int(statistics.endDate.timeIntervalSince1970)
                resultInfo.values = []
                
                if let maxQuantity = statistics.maximumQuantity() {
                    floatArray.append(maxQuantity.doubleValue(for: unit))
                    resultInfo.values = floatArray
                    floatArray.removeAll()
                } else if let minimumQuantity = statistics.minimumQuantity() {
                    floatArray.append(minimumQuantity.doubleValue(for: unit))
                    resultInfo.values = floatArray
                    floatArray.removeAll()
                } else if let averageQuantity = statistics.averageQuantity() {
                    floatArray.append(averageQuantity.doubleValue(for: unit))
                    resultInfo.values = floatArray
                    floatArray.removeAll()
                } else if let sum = statistics.sumQuantity() {
                    let roundedValue = round(sum.doubleValue(for: unit) * 100) / 100.0
                    floatArray.append(roundedValue)
                    resultInfo.values = floatArray
                    floatArray.removeAll()
                }
                
                block+=1
                resultInfoArray.append(resultInfo)
            }
           
        }
        
        var result = AdvancedQueryResponse()
        result.results = resultInfoArray
        return result
        
    }
    
    func processSimpleQueryResult(result: [HKCorrelation],
                                  firstType: HKQuantityType,
                                  secondType:HKQuantityType) -> AdvancedQueryResponse {
        
        var block = 0
        var resultInfo = AdvancedQueryResponseBlock()
        var resultInfoArray = [AdvancedQueryResponseBlock]()
        var floatArray = [Double]()
        
        for item in result {
            let bloodPressureSystolic = (item.objects(for: firstType))
            let bloodPressureDiastolic = (item.objects(for: secondType))

            if let data = bloodPressureSystolic.first {
                let obj = data as? HKQuantitySample

                if let startDate = obj?.startDate {
                    resultInfo.startDate = Int(startDate.timeIntervalSince1970)
                }
                if let endDate = obj?.endDate {
                    resultInfo.endDate = Int(endDate.timeIntervalSince1970)
                }
                if let quantity = obj?.quantity.doubleValue(for: HKUnit.millimeterOfMercury()) {
                    floatArray.append(quantity)
                }
            }

            if let dataDiastolic = bloodPressureDiastolic.first {
                let obj = dataDiastolic as? HKQuantitySample
                if let quantity = obj?.quantity.doubleValue(for: HKUnit.millimeterOfMercury()) {
                    floatArray.append(quantity)
                }
            }
            resultInfo.block = block
            resultInfo.values = floatArray

        }
        resultInfoArray.append(resultInfo)
        block+=1
        
        var result = AdvancedQueryResponse()
        result.results = resultInfoArray
        return result
    }

    func advancedQuery(variable: String,
                       startDate: Date,
                       endDate: Date,
                       timeUnit: String,
                       operationType: String,
                       mostRecent: Bool,
                       timeUnitLength: Int,
                       completion: @escaping(AdvancedQueryResponse?, NSError?) -> Void) {
    
        if let error = self.isHealthDataAvailable() {
            completion(nil, error)
        }
        
        guard let type = self.HKTypes.allVariablesDict[variable] else {
            completion(nil, HealthKitErrors.variableNotAvailable as NSError)
            return
        }
    
        if let objectType = type.first?.objectType {
            let authStatus = self.store?.checkAuthorizationStatus(for: objectType)
            if authStatus == .notDetermined {
                completion(nil, HealthKitErrors.variableNotAuthorized as NSError)
                return
            }
        }

        guard let unit = type.first?.unit else {
            let error = HealthKitErrors.variableNotAvailable
            completion(nil, error as NSError)
            return
        }
        
        let anchorDate = Calendar.current.date(from: self.getCalendarComponent(date: startDate))!
        let types = self.HKTypes.allVariablesDict[variable]
        var HKOptions = self.getStatisticOptions(operationType: operationType)
        
        if !mostRecent {
            if let optionsAllowed = type.first?.optionsAllowed {
                if !optionsAllowed.contains(HKOptions) {
                    completion(nil, HealthKitErrors.operationNotAllowed as NSError)
                    return
                }
            }
        }
        var interval = self.getInterval(timeUnit: timeUnit, timeUnitLength: timeUnitLength)
            
        if let correlationType = types?.first?.correlationType {
            
            guard let startDate = NSCalendar.current.date(from: self.getCalendarComponent(date: startDate)) else {
                return completion(nil, HealthKitErrors.errorWhileReading as NSError)
            }
            
            guard let endDate = NSCalendar.current.date(from: self.getCalendarComponent(date: endDate)) else {
                return completion(nil, HealthKitErrors.errorWhileReading as NSError)
            }
            
            var predicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate, options: [.strictEndDate])
            var limit = 0
            
            if mostRecent {
                limit = 1
                predicate = HKQuery.predicateForSamples(withStart: Date.distantPast, end: endDate, options: [.strictEndDate])
            }
            
            self.store?.executeSimpleQuery(sample:correlationType,
                                           predicate: predicate,
                                           limit: limit,
                                           sortDescriptors: [NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)]) { results in
                switch results {
                    case .failure:
                        completion(nil, HealthKitErrors.errorWhileReading as NSError)
                    case .success(let data):
                        
                        if let firstType = types?[0].quantityType,
                            let secondType = types?[1].quantityType,
                            let dataList = data
                        {
                            let result = self.processSimpleQueryResult(result: dataList,
                                                                       firstType: firstType,
                                                                       secondType: secondType)
                            
                            completion(result, nil)
                            
                        } else {
                            return completion(nil, HealthKitErrors.errorWhileReading as NSError)
                        }
                }
            }
            
        } else if let type = types?.first {
            
            if let quantityType = type.quantityType {
                                
                var newStartDate = Date()
                if mostRecent {
                    HKOptions = .mostRecent
                    newStartDate = Date.distantPast
                    interval.year = 200
                } else {
                    newStartDate = startDate
                }
                
                self.store?.executeAdvancedQuery(quantityType: quantityType,
                                                 options: HKOptions,
                                                 anchorDate: anchorDate,
                                                 interval: interval,
                                                 newStartDate: newStartDate) { results in
                    switch results {
                        case .failure:
                            completion(nil, HealthKitErrors.errorWhileReading as NSError)
                        case .success(let data):
                            if let data = data {
                                let result = self.processAdvancedQueryResult(newStartDate: newStartDate,
                                                                             endDate: endDate,
                                                                             mostRecent: mostRecent,
                                                                             unit: unit,
                                                                             result: data)
                                completion(result, nil)
                            } else {
                                completion(nil, nil)
                            }
                            
                    }
                }
            }
        }
    }
    
}
