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
                HKOptions = [.cumulativeSum]
        }
        return HKOptions
    }
    
    func getInterval(timeUnit:String) -> DateComponents {
        var interval = DateComponents()
        switch timeUnit {
            case TimeUnit.milliseconds.rawValue:
                interval.second = 1000
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
        
        let variable = "BLOOD_PRESSURE"
        
        guard let type = HKTypes.allVariablesQuantityDictToQuery[variable] else {
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
        
        let types = HKTypes.allVariablesQuantityDictToQuery[variable]
        
        let HKOptions = getStatisticOptions(operationType: operationType)
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
            
            let predicate = HKQuery.predicateForSamples(withStart: startDate, end: endDate, options: [])
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
                    
            let query = HKStatisticsCollectionQuery(quantityType: type.quantityType,
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
                    
                    if let maxQuantity = statistics.maximumQuantity() {
                        resultInfo.block = block
                        resultInfo.startDate = Int(statistics.startDate.timeIntervalSince1970)
                        resultInfo.endDate = Int(statistics.endDate.timeIntervalSince1970)
                        floatArray.append(Float(maxQuantity.doubleValue(for: unit)))
                        resultInfo.values = floatArray
                        floatArray.removeAll()

                        let steps = maxQuantity.doubleValue(for: unit)
                        print("heart rate: \(steps), starDate: \(statistics.startDate), endDate: \(statistics.endDate)")

                        resultInfoArray.append(resultInfo)
                        
                    } else if let minimumQuantity = statistics.minimumQuantity() {
                        resultInfo.block = block
                        resultInfo.startDate = Int(statistics.startDate.timeIntervalSince1970)
                        resultInfo.endDate = Int(statistics.endDate.timeIntervalSince1970)
                        floatArray.append(Float(minimumQuantity.doubleValue(for: unit)))
                        resultInfo.values = floatArray
                        floatArray.removeAll()

                        let steps = minimumQuantity.doubleValue(for: unit)
                        print("heart rate: \(steps), starDate: \(statistics.startDate), endDate: \(statistics.endDate)")
                        resultInfoArray.append(resultInfo)
                        
                    } else if let averageQuantity = statistics.averageQuantity() {
                        resultInfo.block = block
                        resultInfo.startDate = Int(statistics.startDate.timeIntervalSince1970)
                        resultInfo.endDate = Int(statistics.endDate.timeIntervalSince1970)
                        
                        floatArray.append(Float(averageQuantity.doubleValue(for: unit)))
                        resultInfo.values = floatArray
                        floatArray.removeAll()

                        let steps = averageQuantity.doubleValue(for: unit)
                        print("heart rate: \(steps), starDate: \(statistics.startDate), endDate: \(statistics.endDate)")

                        resultInfoArray.append(resultInfo)
                        block+=1
                        
                    } else if let sum = statistics.sumQuantity() {

                        resultInfo.block = block
                        resultInfo.startDate = Int(statistics.startDate.timeIntervalSince1970)
                        resultInfo.endDate = Int(statistics.endDate.timeIntervalSince1970)
                        
                        floatArray.append(Float(sum.doubleValue(for: unit)))
                        resultInfo.values = floatArray
                        floatArray.removeAll()

                        let steps = sum.doubleValue(for: unit)
                        print("Amount of steps: \(steps), starDate: \(statistics.startDate), endDate: \(statistics.endDate)")

                        resultInfoArray.append(resultInfo)
                        block+=1
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


