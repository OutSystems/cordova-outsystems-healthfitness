//
//  UserHealthProfile.swift
//  UserHealthProfile
//
//  Created by Andre Grillo on 31/08/2021.
//

import HealthKit

class UserHealthProfile {
  
  var age: Int?
  var biologicalSex: HKBiologicalSex?
  var bloodType: HKBloodType?
  var heightInMeters: Double?
  var weightInKilograms: Double?
  var error: Bool = false
  var errorMessage: String = ""
  
  var bodyMassIndex: Double? {
    
    guard let weightInKilograms = weightInKilograms,
      let heightInMeters = heightInMeters,
      heightInMeters > 0 else {
        return nil
    }
    
    return (weightInKilograms/(heightInMeters*heightInMeters))
  }
}
