import Foundation

typealias CompletionHandler = () throws -> HealthKitErrors?

public enum HealthKitErrors : Int, CustomNSError, LocalizedError {
    case variableNotAvailable = 100
    case variableNotAuthorized = 101
    case operationNotAllowed = 102
    case errorWhileReading = 103
    case errorWhileWriting = 104
    case variableHasWriteDenied = 105
    case badParameterType = 106
    case authorizationError = 107
    case notAvailableOnDevice = 108
    case unitNotAvailable = 109
    case featureNotAvailable = 110
    
    var description: String {
        switch self {
            case .notAvailableOnDevice:
                return "HealthKit not available on device."
            case .variableNotAvailable:
                return "Variable not available."
            case .variableHasWriteDenied:
                return "Variable has write denied."
            case .authorizationError:
                return "Authorization error."
            case .variableNotAuthorized:
                return "Variable not authorized."
            case .operationNotAllowed:
                return "Operation not allowed."
            case .featureNotAvailable:
                return "Feature not available on device."
            case .badParameterType:
                return "Invalid parameter."
            case .errorWhileReading:
                return "Error while reading data."
            case .errorWhileWriting:
                return "Error while writing data."
            case .unitNotAvailable:
                return "Variable not available."
        }
    }
    
    public var errorDescription: String? {
        return description == "" ? NSLocalizedString(String(rawValue), comment: "") : description
    }
        
}
