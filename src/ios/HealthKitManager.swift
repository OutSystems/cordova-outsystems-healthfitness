import HealthKit

class HealthKitManager {
    
    var healthKitTypesToRead = Set<HKObjectType>()
    var healthKitTypesToWrite = Set<HKSampleType>()
    var HKTypes = HealthKitTypes()
        
    func isValidVariable(dict:[String: [HealthKitVariable]], variable:String) -> Bool {
        let filtered = dict.filter { $0.key == variable }
        return !filtered.isEmpty
    }
    
    func parseCustomPermissons(customPermissions:String) -> Bool {
        if let permissions = customPermissions.decode(string: customPermissions) as PermissionsArray?{
            for element in permissions {
                let variable = element.variable
                let existVariable = isValidVariable(dict: HKTypes.allVariablesDict, variable: variable)
                if existVariable {
                    for (_, variables) in HKTypes.allVariablesDict {
                        if variables.count > 1 {
                            for item in variables {
                                if element.accessType == AccessTypeEnum.write.rawValue {
                                    healthKitTypesToWrite.insert(item.sampleType)
                                } else if (element.accessType == AccessTypeEnum.readWrite.rawValue) {
                                    healthKitTypesToWrite.insert(item.sampleType)
                                    healthKitTypesToRead.insert(item.objectType)
                                } else {
                                    healthKitTypesToRead.insert(item.objectType)
                                }
                            }
                        } else {
                            if let variable = variables.first {
                                if element.accessType == AccessTypeEnum.write.rawValue {
                                    healthKitTypesToWrite.insert(variable.sampleType)
                                } else if (element.accessType == AccessTypeEnum.readWrite.rawValue) {
                                    healthKitTypesToWrite.insert(variable.sampleType)
                                    healthKitTypesToRead.insert(variable.objectType)
                                } else {
                                    healthKitTypesToRead.insert(variable.objectType)
                                }
                            }
                        }
                    }
                } else {
                    return false
                }
            }
        }
        
        return true
    }
    
    func fillPermissionSetWithVariables(dict: [String: [HealthKitVariable]],
                          groupPermissions:GroupPermissions) {
                
        for (_, variables) in dict {
            if variables.count > 1 {
                for item in variables {
                    if groupPermissions.accessType == AccessTypeEnum.write.rawValue {
                        healthKitTypesToWrite.insert(item.sampleType)
                    } else if (groupPermissions.accessType == AccessTypeEnum.readWrite.rawValue) {
                        healthKitTypesToWrite.insert(item.sampleType)
                        healthKitTypesToRead.insert(item.objectType)
                    } else {
                        healthKitTypesToRead.insert(item.objectType)
                    }
                }
            } else {
                if let variable = variables.first {
                    if groupPermissions.accessType == AccessTypeEnum.write.rawValue {
                        healthKitTypesToWrite.insert(variable.sampleType)
                    } else if (groupPermissions.accessType == AccessTypeEnum.readWrite.rawValue) {
                        healthKitTypesToWrite.insert(variable.sampleType)
                        healthKitTypesToRead.insert(variable.objectType)
                    } else {
                        healthKitTypesToRead.insert(variable.objectType)
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
                            completion: @escaping (Bool, NSError?) -> Void) {
        
        var isAuthorizationValid = true
        
        if let error = self.isHealthDataAvailable() {
            completion(false, error)
        }
        
        let all = allVariables.decode(string: allVariables) as GroupPermissions
        if all.isActive {
            self.fillPermissionSetWithVariables(dict: HKTypes.allVariablesDict,
                                  groupPermissions: all)
        }
        
        let fitness = fitnessVariables.decode(string: fitnessVariables) as GroupPermissions
        if fitness.isActive {
            self.fillPermissionSetWithVariables(dict: HKTypes.fitnessVariablesDict,
                                                groupPermissions: fitness)
        }
        
        let health = healthVariables.decode(string: healthVariables) as GroupPermissions
        if health.isActive {
            self.fillPermissionSetWithVariables(dict: HKTypes.healthVariablesDict,
                                                groupPermissions: health)
        }
        
        let profile = profileVariables.decode(string: profileVariables) as GroupPermissions
        if profile.isActive {
            self.fillPermissionSetWithVariables(dict: HKTypes.profileVariablesDict,
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
    
    func getLastRecord(variable:String,
                       completion: @escaping (Double?, NSError?) -> Void) {
        
        if let error = self.isHealthDataAvailable() {
            completion(nil, error)
        }
        
        guard let type = HKTypes.allVariablesDict[variable] else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(nil, error as NSError)
            return
        }
        
        guard let sampleType = type.first?.sampleType else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(nil, error as NSError)
            return
        }
        
        guard let unit = type.first?.unit else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(nil, error as NSError)
            return
        }
    
        let mostRecentPredicate = HKQuery.predicateForSamples(withStart: Date.distantPast,
                                                              end: Date(),
                                                              options: .strictEndDate)
        let sortDescriptor = NSSortDescriptor(key: HKSampleSortIdentifierStartDate,
                                              ascending: false)
        let limit = 1
        let sampleQuery = HKSampleQuery(sampleType: sampleType,
                                        predicate: mostRecentPredicate,
                                        limit: limit,
                                        sortDescriptors: [sortDescriptor]) { (query, samples, error) in
            
            if let error = error {
                switch (error as NSError).code {
                case 1:
                    completion(nil, HealthKitErrors.notAvailableOnDevice as NSError)
                case 5:
                    completion(nil, HealthKitErrors.notAuthorizedByUser as NSError)
                default:
                    completion(nil, error as NSError)
                }
            }
            
            guard let samples = samples else {
                return
            }
            let obj = samples.first as? HKQuantitySample
            let quant = obj?.quantity.doubleValue(for: unit)
            completion(quant, nil)
            
        }
        
        HKHealthStore().execute(sampleQuery)
        
    }
    
    func writeData(variable: String,
                   value: String,
                   completion: @escaping (Bool, NSError?) -> Void) {
        
        if let error = self.isHealthDataAvailable() {
            completion(false, error)
        }
                
        guard let type = HKTypes.allVariablesDict[variable] else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(false, error as NSError)
            return
        }
        
        guard let unit = type.first?.unit else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(false, error as NSError)
            return
        }
        
        guard let quantityType = type.first?.quantityType else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(false, error as NSError)
            return
        }
        
        
        if let val = Double(value) {
            let variableQuantity = HKQuantity(unit: unit, doubleValue: val)
            
            let countSample = HKQuantitySample(type: quantityType,
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
            default:
                HKOptions = []
        }
        return HKOptions
    }
    
    func getInterval(timeUnit:String) -> DateComponents {
        var interval = DateComponents()
        switch timeUnit {
            case TimeUnit.milliseconds.rawValue:
                interval.second = 1
            case TimeUnit.seconds.rawValue:
                interval.second = 1
            case TimeUnit.minute.rawValue:
                interval.minute = 1
            case TimeUnit.hour.rawValue:
                interval.hour = 1
            case TimeUnit.day.rawValue:
                interval.day = 1
            case TimeUnit.week.rawValue:
                interval.weekOfYear = 1
            case TimeUnit.month.rawValue:
                interval.month = 1
            case TimeUnit.year.rawValue:
                interval.year = 1
            default:
                interval.day = 1
        }
        return interval
    }

    func getData(variable: String,
                 startDate: Date,
                 endDate: Date,
                 timeUnit: String,
                 operationType: String,
                 completion: @escaping(AdvancedQueryResponse?, NSError?) -> Void) {
        
//        let variable = "BLOOD_PRESSURE"
        
        guard let type = HKTypes.allVariablesDict[variable] else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(nil, error as NSError)
            return
        }

        guard let unit = type.first?.unit else {
            let error = HealthKitErrors.dataTypeNotAvailable
            completion(nil, error as NSError)
            return
        }
        
        let sortDescriptors = [
            NSSortDescriptor(key: HKSampleSortIdentifierStartDate, ascending: false)
        ]
        
        let anchorComponents = getCalendarComponent(date: startDate)
        let anchorDate = Calendar.current.date(from: anchorComponents)!
        
        let types = HKTypes.allVariablesDict[variable]
        
        var HKOptions = getStatisticOptions(operationType: operationType)
        let interval = getInterval(timeUnit: timeUnit)
        
        var block = 0
        var resultInfo = AdvancedQueryResponseBlock()
        var resultInfoArray = [AdvancedQueryResponseBlock]()
        var floatArray = [Float]()
        
        if types!.count > 1 {
            guard let type = types?.first?.correlationType else { return }
            
            let calendar = NSCalendar.current
            let componentsStart = getCalendarComponent(date: startDate)
            let componentsEnd = getCalendarComponent(date: endDate)
            
            guard let startDate = calendar.date(from: componentsStart) else {
                fatalError("*** Unable to create the start date ***")
            }
            
            guard let endDate = calendar.date(from: componentsEnd) else {
                fatalError("*** Unable to create the start date ***")
            }
            
            let predicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate, options: [.strictStartDate])
            
            let sampleQuery = HKSampleQuery(sampleType: type, predicate: predicate, limit: 0, sortDescriptors: sortDescriptors)
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
                                    floatArray.append(Float(quantity))
                                }
                            }

                            if let dataDiastolic = bloodPressureDiastolic.first {
                                let obj = dataDiastolic as? HKQuantitySample
                                if let quantity = obj?.quantity.doubleValue(for: HKUnit.millimeterOfMercury()) {
                                    floatArray.append(Float(quantity))
                                }
                            }
                            resultInfo.block = block
                            resultInfo.values = floatArray

                        }
                        resultInfoArray.append(resultInfo)
                        block+=1
                        print(resultInfoArray)
                    }
                }
                var result = AdvancedQueryResponse()
                result.results = resultInfoArray
                completion(result, nil)
            }
            HKHealthStore().execute(sampleQuery)

        } else if let type = types?.first {
            
            if let quantityType = type.quantityType {
                
                if let defaultOption = type.defaultOption {
                    HKOptions = defaultOption
                }
                
                let query = HKStatisticsCollectionQuery(quantityType: quantityType,
                                                        quantitySamplePredicate: nil,
                                                        options: HKOptions,
                                                        anchorDate: anchorDate,
                                                        intervalComponents: interval)
                query.initialResultsHandler = { _, results, error in
                    
                    if error != nil {
                        completion(nil, error as NSError?)
                        return
                    }
                        
                    guard let results = results else {
                        completion(nil, HealthKitErrors.noResultsForQuery as NSError)
                        return
                    }

                    results.enumerateStatistics(from: startDate, to: endDate) { statistics, _ in
                        resultInfo.block = block
                        resultInfo.startDate = Int(statistics.startDate.timeIntervalSince1970)
                        resultInfo.endDate = Int(statistics.endDate.timeIntervalSince1970)
                        resultInfo.values = []
                        if let maxQuantity = statistics.maximumQuantity() {
                            floatArray.append(Float(maxQuantity.doubleValue(for: unit)))
                            resultInfo.values = floatArray
                            floatArray.removeAll()
                        } else if let minimumQuantity = statistics.minimumQuantity() {
                            floatArray.append(Float(minimumQuantity.doubleValue(for: unit)))
                            resultInfo.values = floatArray
                            floatArray.removeAll()
                        } else if let averageQuantity = statistics.averageQuantity() {
                            floatArray.append(Float(averageQuantity.doubleValue(for: unit)))
                            resultInfo.values = floatArray
                            floatArray.removeAll()
                        } else if let sum = statistics.sumQuantity() {
                            floatArray.append(Float(sum.doubleValue(for: unit)))
                            resultInfo.values = floatArray
                            floatArray.removeAll()
                        }
                        block+=1
                        resultInfoArray.append(resultInfo)
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


