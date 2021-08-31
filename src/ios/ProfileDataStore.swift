//
//  ProfileDataStore.swift
//  ProfileDataStore
//
//  Created by Andre Grillo on 31/08/2021.
//

import HealthKit

class ProfileDataStore {
  
    class func getAgeSexAndBloodType() throws -> (age: Int,
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

        
}
