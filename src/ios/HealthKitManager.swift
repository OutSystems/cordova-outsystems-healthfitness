import HealthKit

typealias CompletionHandler = () throws -> HealthKitErrors?

class HealthKitManager {
    
    var healthKitTypesToRead = Set<HKObjectType>()
    var healthKitTypesToWrite = Set<HKSampleType>()
    var HKTypes = HealthKitTypes()
    
    func parseCustomPermissons(customPermissions:String) -> Bool {
        if let permissions = customPermissions.decode(string: customPermissions) as PermissionsArray?{
            for element in permissions {
                let variable = element.variable
                
                if let type = HKTypes.allVariablesDict[variable] {
                    if type.count > 1 {
                        for item in type {
                            let sampleType = item.sampleType
                            let objectType = item.objectType
                            
                            self.fillSets(accessType: element.accessType,
                                          sampleType: sampleType,
                                          objectType: objectType)
                        }
                        
                    } else {
                        guard let sampleType = type.first?.sampleType else { return false }
                        guard let objectType = type.first?.objectType else { return false }
                        
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
                    
                    let sampleType = item.sampleType
                    let objectType = item.objectType
                    
                    self.fillSets(accessType: groupPermissions.accessType,
                                  sampleType: sampleType,
                                  objectType: objectType)
                }
                
            } else {
                
                if let variable = variables.first {
                    let sampleType = variable.sampleType
                    let objectType = variable.objectType
                    
                    self.fillSets(accessType: groupPermissions.accessType,
                                  sampleType: sampleType,
                                  objectType: objectType)
                }
                
            }
        }
    }
    
    func setPermissionsFor(variable: HealthKitVariable){
        let sampleType = variable.sampleType
        let objectType = variable.objectType
     
        let authStatus = HKHealthStore().authorizationStatus(for: objectType)
        if (authStatus == .sharingAuthorized) {
            healthKitTypesToWrite.insert(sampleType)
        }
        
        let authStatusRead = HKHealthStore().authorizationStatus(for: objectType)
        if !(authStatusRead == .notDetermined) {
            healthKitTypesToRead.insert(objectType)
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
        
        DispatchQueue.global(qos: .default).async {
        
            var isAuthorizationValid = true
            
            if let error = self.isHealthDataAvailable() {
                completion(false, error)
            }
            
            self.fillSetsWithHistory()
            
            let all = allVariables.decode(string: allVariables) as GroupPermissions
            if all.isActive {
                self.fillPermissionSetWithVariables(dict: self.HKTypes.allVariablesDict,
                                      groupPermissions: all)
            }

            let fitness = fitnessVariables.decode(string: fitnessVariables) as GroupPermissions
            if fitness.isActive {
                self.fillPermissionSetWithVariables(dict: self.HKTypes.fitnessVariablesDict,
                                                    groupPermissions: fitness)
            }

            let health = healthVariables.decode(string: healthVariables) as GroupPermissions
            if health.isActive {
                self.fillPermissionSetWithVariables(dict: self.HKTypes.healthVariablesDict,
                                                    groupPermissions: health)
            }

            let profile = profileVariables.decode(string: profileVariables) as GroupPermissions
            if profile.isActive {
                self.fillPermissionSetWithVariables(dict: self.HKTypes.profileVariablesDict,
                                                    groupPermissions: profile)
            }

            let permissonsOK = self.parseCustomPermissons(customPermissions: customPermissions)
            if !permissonsOK {
                isAuthorizationValid = false
                completion(false, HealthKitErrors.variableNotAvailable as NSError)
            }
            
            if (isAuthorizationValid) {

                self.requestAuthorization(setToWrite:self.healthKitTypesToWrite,
                                          setToRead:self.healthKitTypesToRead) { [self] (success, error) in

                    self.healthKitTypesToWrite.removeAll()
                    self.healthKitTypesToRead.removeAll()
                    
                    if (error != nil) {
                        return completion(false, error as NSError?)
                    }
                    
                    if success {
                        completion(success,error as NSError?)
                    }
                    
                }
                
            }
            
        }
        
    }
    
    func requestAuthorization(setToWrite: Set<HKSampleType>?,
                              setToRead: Set<HKObjectType>?,
                               completion: @escaping (Bool, NSError?) -> Void) {
        
        HKHealthStore().requestAuthorization(toShare: setToWrite,
                                                read: setToRead) { (success, error) in
            
            if (error != nil) {
                return completion(false, HealthKitErrors.authorizationError as NSError)
            }
            
            if success {
                completion(success,error as NSError?)
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
                   completion: @escaping (_ inner: @escaping CompletionHandler) -> Void) {
        
        DispatchQueue.global(qos: .default).async {
            
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
                let authStatus = HKHealthStore().authorizationStatus(for: objectType)
                if authStatus == .sharingDenied {
                    completion ( { throw HealthKitErrors.variableHasWriteDenied } )
                    return
                }
            }
            
            var variableQuantity:HKQuantity
            
            if let val = value {
                
                if unit == HKUnit.percent() {
                    let valuePercent = (val/100)
                    variableQuantity = HKQuantity(unit: unit, doubleValue: valuePercent)
                } else {
                    variableQuantity = HKQuantity(unit: unit, doubleValue: val)
                }
                
                let countSample = HKQuantitySample(type: quantityType,
                                               quantity: variableQuantity,
                                                  start: Date(),
                                                    end: Date())
                
                HKHealthStore().save(countSample) { (success, error) in
                    
                    if let error = error {
                        switch (error as NSError).code {
                        case 1:
                            completion ({ throw HealthKitErrors.notAvailableOnDevice })
                        case 5:
                            completion ({ throw HealthKitErrors.authorizationError })
                        default:
                            completion ({ throw HealthKitErrors.errorWhileWriting })
                        }
                        
                    } else {
                        completion ( { nil } )
                    }
                }
            }
        }
        
    }

    func advancedQuery(variable: String,
                       startDate: Date,
                       endDate: Date,
                       timeUnit: String,
                       operationType: String,
                       mostRecent: Bool,
                       timeUnitLength: Int,
                       completion: @escaping(AdvancedQueryResponse?, NSError?) -> Void) {
        
        DispatchQueue.global(qos: .default).async {
        
            if let error = self.isHealthDataAvailable() {
                completion(nil, error)
            }
            
            guard let type = self.HKTypes.allVariablesDict[variable] else {
                completion(nil, HealthKitErrors.variableNotAvailable as NSError)
                return
            }
        
            if let objectType = type.first?.objectType {
                let authStatus = HKHealthStore().authorizationStatus(for: objectType)
                if authStatus == .notDetermined {
                    completion(nil, HealthKitErrors.variableNotAuthorized as NSError)
                }
            }

            guard let unit = type.first?.unit else {
                let error = HealthKitErrors.variableNotAvailable
                completion(nil, error as NSError)
                return
            }
            
            let sortDescriptors = [
                NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)
            ]
            
            let anchorComponents = self.getCalendarComponent(date: startDate)
            let anchorDate = Calendar.current.date(from: anchorComponents)!
            
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
            
            var block = 0
            var resultInfo = AdvancedQueryResponseBlock()
            var resultInfoArray = [AdvancedQueryResponseBlock]()
            var floatArray = [Double]()
            
            if types!.count > 1 {
                guard let type = types?.first?.correlationType else { return }
                
                let calendar = NSCalendar.current
                let componentsStart = self.getCalendarComponent(date: startDate)
                let componentsEnd = self.getCalendarComponent(date: endDate)
                
                guard let startDate = calendar.date(from: componentsStart) else {
                    fatalError("*** Unable to create the start date ***")
                }
                
                guard let endDate = calendar.date(from: componentsEnd) else {
                    fatalError("*** Unable to create the start date ***")
                }
                
                var predicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate, options: [.strictEndDate])
                var limit = 0
                
                if mostRecent {
                    limit = 1
                    predicate = HKQuery.predicateForSamples(withStart: Date.distantPast, end: endDate, options: [.strictEndDate])
                } else {
                    predicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate, options: [.strictEndDate])
                }
                
                let sampleQuery = HKSampleQuery(sampleType: type, predicate: predicate, limit: limit, sortDescriptors: sortDescriptors)
                    { (sampleQuery, results, error ) -> Void in

                    if let dataList = results as? [HKCorrelation] {
                        if !dataList.isEmpty {
                            for item in dataList {
                                let bloodPressureSystolic = (item.objects(for: (types?[0].quantityType)!))
                                let bloodPressureDiastolic = (item.objects(for: (types?[1].quantityType)!))

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
                        }
                    }
                    var result = AdvancedQueryResponse()
                    result.results = resultInfoArray
                    completion(result, nil)
                }
                HKHealthStore().execute(sampleQuery)

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
                    
                    let query = HKStatisticsCollectionQuery(quantityType: quantityType,
                                                            quantitySamplePredicate: nil,
                                                            options: HKOptions,
                                                            anchorDate: anchorDate,
                                                            intervalComponents: interval)
                    query.initialResultsHandler = { _, results, error in
                        
                        if error != nil {
                            completion(nil, HealthKitErrors.errorWhileReading as NSError)
                            return
                        }
                            
                        guard let results = results else {
//                            completion(nil, HealthKitErrors.noResultsForQuery as NSError)
                            return
                        }

                        results.enumerateStatistics(from: newStartDate, to: endDate) { statistics, _ in
                            
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
                        completion(result, nil)
                    }
                    
                    HKHealthStore().execute(query)
                }
                    
            }
                
        }
    }
}
