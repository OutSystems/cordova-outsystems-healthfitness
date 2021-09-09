//
//  InterfacePluginHealth.swift
//  iOS health Test App
//
//  Created by Carlos Correa on 08/09/2021.
//

import Foundation

protocol IOSPlatformInterface {
    
    func sendResult(result:String,error:String,callBackID:String)
    
}

