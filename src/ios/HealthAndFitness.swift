//
//  HealthAndFitness.swift
//  HealthAndFitness
//
//  Created by Andre Grillo on 30/08/2021.
//

import Foundation

@objc(HealthAndFitness) class HealthAndFitness: CDVPlugin {
    
    var callbackId:String=""
    
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
        let profileDataStore = ProfileDataStore()
        
        do {
            let userAgeSexAndBloodType = try profileDataStore.getAgeSexAndBloodType()
            userHealthProfile.age = userAgeSexAndBloodType.age
            
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
        
        //MARK: TODO: Return JSON as message
        print("HealthKit getAgeSexAndBloodType successful. Age: \(userHealthProfile.age), Biological Sex \(userHealthProfile.biologicalSex), Blood Type: \(userHealthProfile.bloodType)")
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "HealthKit getAgeSexAndBloodType successful. Age: \(userHealthProfile.age), Biological Sex \(userHealthProfile.biologicalSex), Blood Type: \(userHealthProfile.bloodType)")
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
        
    }
    
    
    @objc(getHeartRates:)
    func getHeartRates(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        let profileDataStore = ProfileDataStore()
        profileDataStore.getHeartRates() { result,error in
            self.sendResultHeartRateResult(result: result, error: "")
        }
    }    

    @objc(addChangesObserver:)
    func addChangesObserver(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        let profileDataStore = ProfileDataStore()
        profileDataStore.addChangesObserver()
    }

    @objc(saveSteps:)
    func saveSteps(command: CDVInvokedUrlCommand) {
        callbackId = command.callbackId
        let profileDataStore = ProfileDataStore()
        profileDataStore.saveSteps(stepsCountValue: 200, date: Date()) { error in
            
        }
    }
    
    func sendResultHeartRateResult(result:[HeartRateInfo],error:String) {
        var pluginResult = CDVPluginResult (status: CDVCommandStatus_ERROR);
        if error.isEmpty {
            
            //MARK: TODO: Return JSON as message
            let jsonEncoder = JSONEncoder()
            let jsonData = try? jsonEncoder.encode(result)
            let json = String(data: jsonData!, encoding: String.Encoding.utf8)
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: json)
        } else {
            let errorDict = ["code": "0", "message": error]
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorDict);
        }
        self.commandDelegate!.send(pluginResult, callbackId: callbackId);
        
    }
    
    @objc(loadMostRecentHeight:)
    func loadMostRecentHeight(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult()
        
        //1. Use HealthKit to create the Height Sample Type
        guard let heightSampleType = HKSampleType.quantityType(forIdentifier: .height) else {
            print("Height Sample Type is no longer available in HealthKit")
            return
        }
        
        ProfileDataStore.getMostRecentSample(for: heightSampleType) { (sample, error) in
            
            guard let sample = sample else {
                
                if let error = error {
                    print(error.localizedDescription)
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
                    self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
                }
                
                return
            }
            
            //2. Convert the height sample to meters
            let heightInMeters = sample.quantity.doubleValue(for: HKUnit.meter())
            
            //Callback cordova
            print("HealthKit loadMostRecentHeight successful. Most recent height: \(heightInMeters)")
            
            //MARK TODO: Convert result to JSON
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "\(heightInMeters)")
            self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
        }
    }
    
    
    @objc(loadMostRecentWeight:)
    func loadMostRecentWeight(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult()
        
        guard let weightSampleType = HKSampleType.quantityType(forIdentifier: .bodyMass) else {
            print("Body Mass Sample Type is no longer available in HealthKit")
            return
        }
        
        ProfileDataStore.getMostRecentSample(for: weightSampleType) { (sample, error) in
            
            guard let sample = sample else {
                
                if let error = error {
                    print(error.localizedDescription)
                    pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR)
                    self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
                }
                return
            }
            
            let weightInKilograms = sample.quantity.doubleValue(for: HKUnit.gramUnit(with: .kilo))
            
            //MARK TODO: Convert result to JSON
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "\(weightInKilograms)")
            self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
        }
        
    }

        @objc(saveWeight:)
    func saveWeight(command: CDVInvokedUrlCommand) {
        //MARK TODO: Remove the mock below
        let weight: Double = 45
        let date = Date()
        //MARK TODO: Remove the comments below for allowing receiving parameters from Cordova
        // Still need to change the OSHealthFitness.js
        //if let weigth: Double = command.arguments[0] as? Double, let date: Date = (command.arguments[1] as? Date) {
            ProfileDataStore.saveBodyMassSample(bodyMass: weight, date: date)
        //}
        
    }
    
}


