//
//  HealthAndFitness.swift
//  HealthAndFitness
//
//  Created by Andre Grillo on 30/08/2021.
//

import Foundation

@objc(HealthAndFitness) class HealthAndFitness: CDVPlugin {
    
    @objc(initialize:)
    func initialize(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult()
        
        //Request HealthKit Permissions
        HealthKitSetup.authorizeHealthKit { (authorized, error) in
            
            guard authorized else {
                
                let baseMessage = "HealthKit Authorization Failed"
                
                if let error = error {
                    print("\(baseMessage). Reason: \(error.localizedDescription)")
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "\(baseMessage). Reason: \(error.localizedDescription)")
                    self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
                } else {
                    print(baseMessage)
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: baseMessage)
                    self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
                }
                
                return
            }
            
            print("HealthKit Successfully Authorized.")
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "HealthKit Successfully Authorized.")
            self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
        }
    }
    
    @objc(getAgeSexAndBloodType: error:)
    func getAgeSexAndBloodType(command: CDVInvokedUrlCommand) throws {
        var pluginResult = CDVPluginResult()
        let userHealthProfile = UserHealthProfile()
        do {
          let userAgeSexAndBloodType = try ProfileDataStore.getAgeSexAndBloodType()
          userHealthProfile.age = userAgeSexAndBloodType.age
          userHealthProfile.biologicalSex = userAgeSexAndBloodType.biologicalSex
          userHealthProfile.bloodType = userAgeSexAndBloodType.bloodType
          
        } catch let error {
            userHealthProfile.error = true
            userHealthProfile.errorMessage = error.localizedDescription
        }
        
        if userHealthProfile.error {
            print("HealthKit getAgeSexAndBloodType Failed. Reason: \(userHealthProfile.errorMessage)")
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "HealthKit getAgeSexAndBloodType Failed. Reason: \(userHealthProfile.errorMessage)")
            self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
        } else {
            
            if let age = userHealthProfile.age, let bioSex = userHealthProfile.biologicalSex, let bloodType = userHealthProfile.bloodType {
                
                //MARK: TODO: Return JSON as message
                print("HealthKit getAgeSexAndBloodType successful. Age: \(age), Biological Sex \(bioSex), Blood Type: \(bloodType)")
                pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "HealthKit getAgeSexAndBloodType successful. Age: \(age), Biological Sex \(bioSex), Blood Type: \(bloodType)")
                self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
                
            } else {
                print("HealthKit getAgeSexAndBloodType Failed. Some variables returned null")
                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "HealthKit getAgeSexAndBloodType Failed. Some variables returned null")
                self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
            }
            
        }
    }

}
