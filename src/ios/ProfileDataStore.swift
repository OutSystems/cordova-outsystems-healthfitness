//
//  ProfileDataStore.swift
//  ProfileDataStore
//
//  Created by Andre Grillo on 31/08/2021.
//

import HealthKit

class ProfileDataStore: NSObject {

    func getAgeSexAndBloodType() throws -> (age: Int,
                                            biologicalSex: HKBiologicalSex,
                                            bloodType: HKBloodType) {
        
      let healthKitStore = HKHealthStore()
        
        do {
            
            //1. This method throws an error if these data are not available.
            let birthdayComponents =  try healthKitStore.dateOfBirthComponents()
            let biologicalSex =       try healthKitStore.biologicalSex()
            let bloodType =           try healthKitStore.bloodType()
            
            //2. Use Calendar to calculate age.
            let today = Date()
            let calendar = Calendar.current
            let todayDateComponents = calendar.dateComponents([.year],
                                                              from: today)
            let thisYear = todayDateComponents.year!
            let age = thisYear - birthdayComponents.year!
            
            //3. Unwrap the wrappers to get the underlying enum values.
            let unwrappedBiologicalSex = biologicalSex.biologicalSex
            let unwrappedBloodType = bloodType.bloodType
            
            return (age, unwrappedBiologicalSex, unwrappedBloodType)
            
        }
        //MARK: TODO Catch from error
//        catch let error {
//
//        }
    }
    
    
    //let health: HKHealthStore = HKHealthStore()
    let heartRateUnit:HKUnit = HKUnit(from: "count/min")
    let stepCountUnit:HKUnit = HKUnit(from: "count")
    let heartRateType:HKQuantityType = HKQuantityType.quantityType(forIdentifier: HKQuantityTypeIdentifier.heartRate)!
    let stepCountType:HKQuantityType = HKQuantityType.quantityType(forIdentifier: HKQuantityTypeIdentifier.stepCount)!
    var heartRateQuery:HKSampleQuery?
        
        
    func getHeartRates(completion: @escaping([HeartRateInfo], Error?) -> Void) {
        
        let healthKitStore = HKHealthStore()
        //predicate
        let calendar = NSCalendar.current
        let now = NSDate()
        let components = calendar.dateComponents([.year, .month, .day], from: now as Date)
        
        guard let startDate:NSDate = calendar.date(from: components) as NSDate? else { return }
        var dayComponent    = DateComponents()
        dayComponent.day    = 1
        let endDate:NSDate? = calendar.date(byAdding: dayComponent, to: startDate as Date) as NSDate?
        let predicate = HKQuery.predicateForSamples(withStart: startDate as Date, end: endDate as Date?, options: [])

        //descriptor
        let sortDescriptors = [
                                NSSortDescriptor(key: HKSampleSortIdentifierEndDate, ascending: false)
                              ]
        
        heartRateQuery = HKSampleQuery(sampleType: stepCountType, predicate: predicate, limit: 1, sortDescriptors: sortDescriptors, resultsHandler: { (query, results, error) in
            guard error == nil else { print(error?.localizedDescription ?? ""); return }
            
            var heartRateInfoArray = [HeartRateInfo]()
            for (_, sample) in results!.enumerated() {
                guard let currData:HKQuantitySample = sample as? HKQuantitySample else { return }
                
                let heartRateInfo = HeartRateInfo()
                heartRateInfo.quantity = currData.quantity.doubleValue(for: self.stepCountUnit)
                heartRateInfo.quantityType = "\(currData.quantityType)"
                heartRateInfo.startDate = "\(currData.startDate)"
                heartRateInfo.endDate = "\(currData.endDate)"
                heartRateInfo.metadata = "\(String(describing: currData.metadata))"
                heartRateInfo.uuid = "\(currData.uuid)"
                heartRateInfo.sourceRevision = "\(currData.sourceRevision)"
                heartRateInfo.device = "\(String(describing: currData.device))"
                
                heartRateInfoArray.append(heartRateInfo)
                
            }
            completion(heartRateInfoArray,error)

        })
        
        healthKitStore.execute(heartRateQuery!)
     }

    func saveSteps(stepsCountValue: Int,
                                 date: Date,
                                 completion: @escaping (Error?) -> Void) {
            
        guard let stepCountType = HKQuantityType.quantityType(forIdentifier: .stepCount) else {
            fatalError("Step Count Type is no longer available in HealthKit")
        }
        
        let stepsCountUnit:HKUnit = HKUnit.count()
        let stepsCountQuantity = HKQuantity(unit: stepsCountUnit,
                                           doubleValue: Double(stepsCountValue))
        
        let stepsCountSample = HKQuantitySample(type: stepCountType,
                                               quantity: stepsCountQuantity,
                                               start: date,
                                               end: date)
        
        HKHealthStore().save(stepsCountSample) { (success, error) in
            
            if let error = error {
                completion(error)
                print("Error Saving Steps Count Sample: \(error.localizedDescription)")
            } else {
                completion(nil)
                print("Successfully saved Steps Count Sample")
            }
        }
        
    }

     class func getMostRecentSample(for sampleType: HKSampleType,
                                   completion: @escaping (HKQuantitySample?, Error?) -> Swift.Void) {
        
        //1. Use HKQuery to load the most recent samples.
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
            
            //2. Always dispatch to the main thread when complete.
            DispatchQueue.main.async {
                
                guard let samples = samples,
                      let mostRecentSample = samples.first as? HKQuantitySample else {
                          
                          completion(nil, error)
                          return
                      }
                
                completion(mostRecentSample, nil)
            }
        }
        
        HKHealthStore().execute(sampleQuery)
    }

    
}

class HeartRateInfo: Codable{
    var quantity: Double = 0
    var quantityType: String = ""
    var startDate: String = ""
    var endDate: String = ""
    var metadata: String = ""
    var uuid: String = ""
    var sourceRevision: String = ""
    var device: String = ""
}

