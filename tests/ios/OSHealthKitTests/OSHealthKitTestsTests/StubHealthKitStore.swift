//
//  StubHealthKitStore.swift
//  OSHealthKitTestsTests
//
//  Created by Carlos Correa on 08/10/2021.
//

import Foundation
import HealthKit

public class StubHealthKitStore: HealthKitManagerProtocol {
    
    var didExecuteSimpleQuery = false
    var didWriteSteps = false
    var didAdvancedQuery = false
    var currentAuthorizationStatus: HKAuthorizationStatus = .notDetermined
    
    public func requestAuthorization(setToWrite: Set<HKSampleType>?,
                                     setToRead: Set<HKObjectType>?,
                                     completion: @escaping (Bool, NSError?) -> Void) {
        completion(true, nil)
    }
    
    func executeAdvancedQuery(quantityType: HKQuantityType, options: HKStatisticsOptions, anchorDate: Date, interval: DateComponents, newStartDate: Date, completion: @escaping (Result<HKStatisticsCollection?, Error>) -> Void) {
        
        if didAdvancedQuery {
            completion(.success(nil))
        } else {
            completion(.failure(HealthKitErrors.errorWhileReading))
        }
    }
    
    public func executeSimpleQuery(sample: HKSampleType,
                                   predicate: NSPredicate,
                                   limit: Int,
                                   sortDescriptors: [NSSortDescriptor],
                                   completion: @escaping (Result<[HKCorrelation]?, Error>) -> Void) {
        
        if didExecuteSimpleQuery {
            completion(.success(nil))
        } else {
            completion(.failure(HealthKitErrors.errorWhileReading))
        }
    }
    
    
    public func writeData(sample: HKQuantitySample,
                          completion: @escaping (@escaping () throws -> HealthKitErrors?) -> Void) {
        
        if didWriteSteps {
            completion( { nil } )
        } else {
            completion( { throw HealthKitErrors.errorWhileWriting } )
        }
        
    }
    
    public func checkAuthorizationStatus(for type: HKObjectType) -> HKAuthorizationStatus {
        return self.currentAuthorizationStatus
    }
    
    public func setAuthorizationStatus(status:HKAuthorizationStatus) {
        self.currentAuthorizationStatus = status
    }
    
    public func setDidWriteSteps(_ value:Bool) {
        self.didWriteSteps = value
    }

    public func setDidAdvancedQuery(_ value:Bool) {
        self.didAdvancedQuery = value
    }
    
}
