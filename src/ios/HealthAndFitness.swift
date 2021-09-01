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
    
    @objc(getAgeSexAndBloodType:)
    func getAgeSexAndBloodType(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult()
        let userHealthProfile = UserHealthProfile()
        do {
            let userAgeSexAndBloodType = try ProfileDataStore.getAgeSexAndBloodType()
            //            userHealthProfile.age = userAgeSexAndBloodType.age
            //            userHealthProfile.biologicalSex = userAgeSexAndBloodType.biologicalSex
            //            userHealthProfile.bloodType = userAgeSexAndBloodType.bloodType
            
            //            if let age = userHealthProfile.age, let bioSex = userAgeSexAndBloodType.biologicalSex, let bloodType = userAgeSexAndBloodType.bloodType {
            userHealthProfile.age = userAgeSexAndBloodType.age
            //            let bioSex = userAgeSexAndBloodType.biologicalSex
            //            let bloodType = userAgeSexAndBloodType.bloodType
            
            //            var bioSexString: String
            //            var bloodTypeString: String
            
            switch userAgeSexAndBloodType.biologicalSex.rawValue{
            case 0:
                userHealthProfile.biologicalSex = "Not set"
            case 1:
                userHealthProfile.biologicalSex = "Female"
            case 2:
                userHealthProfile.biologicalSex = "Male"
            case 3:
                userHealthProfile.biologicalSex = "Other"
            default:
                userHealthProfile.biologicalSex = "Not set"
            }
            
            switch userAgeSexAndBloodType.bloodType.rawValue {
            case 0:
                userHealthProfile.bloodType = "Not set"
            case 1:
                userHealthProfile.bloodType = "A Positive"
            case 2:
                userHealthProfile.bloodType = "A Negative"
            case 3:
                userHealthProfile.bloodType = "B Positive"
            case 4:
                userHealthProfile.bloodType = "B Negative"
            case 5:
                userHealthProfile.bloodType = "AB Positive"
            case 6:
                userHealthProfile.bloodType = "AB Negative"
            case 7:
                userHealthProfile.bloodType = "O Positive"
            case 8:
                userHealthProfile.bloodType = "O Negative"
            default:
                userHealthProfile.bloodType = "Not Set"
            }
            //            }
            
        } catch let error {
            
            //            userHealthProfile.errorMessage = error.localizedDescription
            
            print("HealthKit getAgeSexAndBloodType Failed. Reason: \(error.localizedDescription)")
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "HealthKit getAgeSexAndBloodType Failed. Reason: \(error.localizedDescription)")
            self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
        }
        
        
        
        //        if userHealthProfile.error {
        //            print("HealthKit getAgeSexAndBloodType Failed. Reason: \(userHealthProfile.errorMessage)")
        //            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "HealthKit getAgeSexAndBloodType Failed. Reason: \(userHealthProfile.errorMessage)")
        //            self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
        //        } else {
        
        //            if let age = userHealthProfile.age, let bioSex = userHealthProfile.biologicalSex?.rawValue, let bloodType = userHealthProfile.bloodType?.rawValue {
        
        //MARK: TODO: Return JSON as message
        print("HealthKit getAgeSexAndBloodType successful. Age: \(userHealthProfile.age), Biological Sex \(userHealthProfile.biologicalSex), Blood Type: \(userHealthProfile.bloodType)")
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "HealthKit getAgeSexAndBloodType successful. Age: \(userHealthProfile.age), Biological Sex \(userHealthProfile.biologicalSex), Blood Type: \(userHealthProfile.bloodType)")
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
        
    }
    //        else {
    //                print("HealthKit getAgeSexAndBloodType Failed. Some variables returned null")
    //                pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "HealthKit getAgeSexAndBloodType Failed. Some variables returned null")
    //                self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    //            }
    
    //        }
}


