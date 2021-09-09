//
//  HealthKitTypes.swift
//  iOS health Test App
//
//  Created by Carlos Correa on 09/09/2021.
//

import Foundation
import HealthKit

// MARK: - HealthTypeEnum
enum HealthTypeEnum: String
{
    case stepCount = "STEPS",
         heartRate = "HEARTRATE",
         bodyMass = "BODYMASS",
         height = "HEIGTH"
}


// MARK: - CustomPermissions
class CustomPermissions: Codable {
    let variable,
        accessType: String

    enum CodingKeys: String, CodingKey {
        case variable = "Variable"
        case accessType = "AccessType"
    }

    init(variable: String, accessType: String) {
        self.variable = variable
        self.accessType = accessType
    }
}

typealias PermissionsArray = [CustomPermissions]



