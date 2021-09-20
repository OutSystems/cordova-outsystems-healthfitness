import Foundation

enum HealthKitErrors : Int, CustomNSError, LocalizedError {
    case notAvailableOnDevice = 100
    case dataTypeNotAvailable = 101
    case notAuthorizedByUser = 102
    case notVariableAuthorized = 103
    
    var description: String {
        switch self {
            case .notAvailableOnDevice:
                return "Health Kit not available on device."
            case .dataTypeNotAvailable:
                return "Variable not available."
            case .notAuthorizedByUser:
                return "Not authorized by the user."
            case .notVariableAuthorized:
                return "Variable authorized by the user."
        }
    }
    
    var errorDescription: String? {
        return description == "" ? NSLocalizedString(String(rawValue), comment: "") : description
    }
        
}
