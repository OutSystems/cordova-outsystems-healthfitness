import Foundation

extension String {
    
    func decode<T: Decodable>(string:String) -> T {
        let data: Data? = string.data(using: .utf8)
        return try! JSONDecoder().decode(T.self, from: data!)
    }
    
}

extension Encodable {
    
    func encode<T: Encodable>(object:T) -> String {
        let encoder = JSONEncoder()
        encoder.outputFormatting = .prettyPrinted
        let data = try! encoder.encode(object)
        return String(data: data, encoding: .utf8)!
    }
    
}
