//
//  AuthorizationErrors.swift
//  iOS health Test App
//
//  Created by Carlos Correa on 10/09/2021.
//

import Foundation

protocol HealthKitAuthorizationProtocol  {
      var description: String { get }
}

enum HealthKitAuthorizationErrors : Int, HealthKitAuthorizationProtocol {
   case notAvailableOnDevice = 100
   case dataTypeNotAvailable = 101
   case notAuthorizedByUser = 102

    var description: String {
    switch self {
        case .notAvailableOnDevice:
            return "Health Kit not available on device."
        case .dataTypeNotAvailable:
            return "Variable not available."
        case .notAuthorizedByUser:
            return "Not authorized by the user."
        }
    }
    
}
