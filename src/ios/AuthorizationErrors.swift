import Foundation

enum HealthKitErrors : Int, CustomNSError, LocalizedError {
    case notAvailableOnDevice = 100
    case dataTypeNotAvailable = 101
    case notAuthorizedByUser = 102
    case variableNotAuthorized = 103
    case noResultsForQuery = 104
    case variableHasWriteDenied = 105
    case operationNotAllowed = 106
    
    var description: String {
        switch self {
            case .notAvailableOnDevice:
                return "Health Kit not available on device."
            case .dataTypeNotAvailable:
                return "Variable not available."
            case .variableHasWriteDenied:
                return "Variable has write denied."
            case .notAuthorizedByUser:
                return "Not authorized by the user."
            case .variableNotAuthorized:
                return "Variable not authorized by the user."
            case .noResultsForQuery:
                return "Query returned no results."
            case .operationNotAllowed:
                return "Operation not allowed."
        }
    }
    
    var errorDescription: String? {
        return description == "" ? NSLocalizedString(String(rawValue), comment: "") : description
    }
        
}
