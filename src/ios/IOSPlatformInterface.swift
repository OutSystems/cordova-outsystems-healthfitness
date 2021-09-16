
import Foundation

protocol IOSPlatformInterface {
    
    func sendResult(result: String?, error: NSError?, callBackID:String)
    
}

