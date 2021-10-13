//
//  OSHealthKitTestsTests.swift
//  OSHealthKitTestsTests
//
//  Created by Carlos Correa on 07/10/2021.
//

import XCTest
@testable import OSHealthKitTests

class OSHealthKitTestsTests: XCTestCase {
    
    private var manager: StubHealthKitStore?
    
    override func setUp() {
        self.manager = StubHealthKitStore()
    }
    
    // MARK: - Permissions Tests
    func test_Given_InvalidVariable_When_RequestingPermissions_Then_VariableNotAvailableError() throws {
        let stub = StubHealthKitStore()
        let testSubject = HealthKitManager(store: stub)

        let customPermissions = "[{\"Variable\":\"TEST\",\"AccessType\":\"READ\"}]"
        let commonObject = "{\"IsActive\":false,\"AccessType\":\"\"}"
        testSubject.authorizeHealthKit(customPermissions: customPermissions,
                                       allVariables: commonObject,
                                       fitnessVariables: commonObject,
                                       healthVariables: commonObject,
                                       profileVariables: commonObject,
                                       summaryVariables: commonObject) { (authorized, error) in
            
            if (error != nil) {
                XCTAssertEqual(error as? HealthKitErrors, .variableNotAvailable)
            } else {
                XCTFail("Did not throw error")
            }
            
        }
    }
    
    // MARK: - WriteData Tests
    func test_Given_InvalidVariable_When_WritingData_Then_VariableNotAvailableError() throws {
        let stub = StubHealthKitStore()
        let testSubject = HealthKitManager(store: stub)
        testSubject.writeData(variable: "Test", value: 10) { (inner: CompletionHandler) -> Void in
            do {
                let _ = try inner()
                XCTFail("Did not throw error")
            } catch let error {
                XCTAssertEqual(error as? HealthKitErrors, .variableNotAvailable)
            }
        }
    }
    
    func test_Given_SharingDeniedVariable_When_WritingData_Then_VariableHasWriteDeniedError() throws {
        let stub = StubHealthKitStore()
        stub.setAuthorizationStatus(status: .sharingDenied)
        let testSubject = HealthKitManager(store: stub)
        testSubject.writeData(variable: "STEPS", value: 10) { (inner: CompletionHandler) -> Void in
            do {
                let _ = try inner()
                XCTFail("Did not throw error")
            } catch let error {
                XCTAssertEqual(error as? HealthKitErrors, .variableHasWriteDenied)
            }
        }
    }
    
    func test_Given_ValidVariableValidValue_When_WritingData_Then_Success() throws {
        let stub = StubHealthKitStore()
        stub.setAuthorizationStatus(status: .sharingAuthorized)
        stub.setDidWriteSteps(value: true)
        
        let testSubject = HealthKitManager(store: stub)
        testSubject.writeData(variable: "BODY_FAT_PERCENTAGE", value: 10) { (inner: CompletionHandler) -> Void in
            do {
                let _ = try inner()
            } catch let error {
                XCTFail("Did throw error" + error.localizedDescription)
            }
        }
    }
    
    func test_Given_ValidVariableValidValue_When_WritingData_Then_SomeError() throws {
        let stub = StubHealthKitStore()
        stub.setAuthorizationStatus(status: .sharingAuthorized)
        stub.setDidWriteSteps(value: false)
        
        let testSubject = HealthKitManager(store: stub)
        testSubject.writeData(variable: "BODY_FAT_PERCENTAGE", value: 10) { (inner: CompletionHandler) -> Void in
            do {
                let _ = try inner()
                XCTFail("Did throw error")
            } catch let error {
                XCTAssertEqual(error as? HealthKitErrors, .errorWhileWriting)
            }
        }
    }
    
    // MARK: AdvancedQuery Tests
    func test_Given_InvalidOperation_When_AdvancedQuery_Then_OperationNotAllowedError() throws {
        let stub = StubHealthKitStore()
        stub.setAuthorizationStatus(status: .sharingAuthorized)
        let testSubject = HealthKitManager(store: stub)
        
        testSubject.advancedQuery(variable: "HEART_RATE",
                                  startDate: Date(),
                                  endDate: Date(),
                                  timeUnit: "DAY",
                                  operationType: "SUM",
                                  mostRecent: false,
                                  timeUnitLength: 0) { result, error in
            
            if let error = error {
                XCTAssertEqual(error as? HealthKitErrors, .operationNotAllowed)
            } else {
                XCTFail("Did not throw error")
            }
        }
        
    }
    
    func test_Given_InvalidVariable_When_AdvancedQuery_Then_VariableNotAvailableError() throws {
        let stub = StubHealthKitStore()
        stub.setAuthorizationStatus(status: .sharingAuthorized)
        let testSubject = HealthKitManager(store: stub)
        
        testSubject.advancedQuery(variable: "Test",
                                  startDate: Date(),
                                  endDate: Date(),
                                  timeUnit: "DAY",
                                  operationType: "SUM",
                                  mostRecent: false,
                                  timeUnitLength: 0) { result, error in
            
            if let error = error {
                XCTAssertEqual(error as? HealthKitErrors, .variableNotAvailable)
            } else {
                XCTFail("Did not throw error")
            }
        }
        
    }
    
    func test_Given_VariableWithoutPermissions_When_SimpleQuery_Then_VariableNotAuthorizedError() throws {
        let stub = StubHealthKitStore()
        stub.setAuthorizationStatus(status: .notDetermined)
        let testSubject = HealthKitManager(store: stub)
        
        testSubject.advancedQuery(variable: "STEPS",
                                  startDate: Date(),
                                  endDate: Date(),
                                  timeUnit: "DAY",
                                  operationType: "SUM",
                                  mostRecent: false,
                                  timeUnitLength: 0) { result, error in
            
            if let error = error {
                XCTAssertEqual(error as? HealthKitErrors, .variableNotAuthorized)
            } else {
                XCTFail("Did not throw error")
            }
        }
        
    }
    
}
