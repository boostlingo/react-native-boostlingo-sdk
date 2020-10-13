import Boostlingo
import UIKit
import TwilioVideo

@objc(BLVideoView)
class BLVideoView: RCTViewManager {

    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }

    override func view() -> UIView! {
        let container = UIView()
        let inner = VideoView()
        inner.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        inner.contentMode = .scaleAspectFill
        container.addSubview(inner)
        return container;
    }
    
    @objc
    func attachAsLocal(_ node: NSNumber) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node)! as! UIView
            let boostlingoSdkModule = self.bridge.module(forName: "BoostlingoSdk") as! BoostlingoSdk
            boostlingoSdkModule.setLocalVideoView(component.subviews[0] as! VideoView)
        }
    }
    
    @objc
    func attachAsRemote(_ node: NSNumber) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node)! as! UIView
            let boostlingoSdkModule = self.bridge.module(forName: "BoostlingoSdk") as! BoostlingoSdk
            boostlingoSdkModule.setRemoteVideoView(component.subviews[0] as! VideoView)
        }
    }
    
    @objc
    func detach(_ node: NSNumber) {
        DispatchQueue.main.async {
            let component = self.bridge.uiManager.view(forReactTag: node)! as! UIView
            let boostlingoSdkModule = self.bridge.module(forName: "BoostlingoSdk") as! BoostlingoSdk
            boostlingoSdkModule.detachVideoView(component.subviews[0] as! VideoView)
        }
    }
}

@objc(BoostlingoSdk)
class BoostlingoSdk: RCTEventEmitter, BLCallDelegate, BLChatDelegate, BLVideoDelegate {

    private var boostlingo: Boostlingo?
    private var hasListeners: Bool = false
    private var remoteVideoView: VideoView?
    private var localVideoView: VideoView?
    
    @objc
    override func supportedEvents() -> [String] {
        return [
            "callDidConnect",
            "callDidDisconnect",
            "callDidFailToConnect",
            "chatConnected",
            "chatDisconnected",
            "chatMessageRecieved",
            "remoteAudioEnabled",
            "remoteAudioDisabled",
            "remoteVideoEnabled",
            "remoteVideoDisabled",
            "remoteAudioPublished",
            "remoteAudioUnpublished",
            "remoteVideoPublished",
            "remoteVideoUnpublished",
            "localVideoViewAttached",
            "remoteVideoViewAttached"]
    }
    
    @objc
    override func startObserving() {
        hasListeners = true
    }
    
    @objc
    override func stopObserving() {
        hasListeners = false
    }
    
    func setLocalVideoView(_ localVideoView: VideoView?) {
        self.localVideoView = localVideoView
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "localVideoViewAttached", body: nil)
            }
        }
    }
    
    func setRemoteVideoView(_ remoteVideoView: VideoView?) {
        self.remoteVideoView = remoteVideoView
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "remoteVideoViewAttached", body: nil)
            }
        }
    }
    
    func detachVideoView(_ videoView: VideoView?) {
        if localVideoView == videoView {
            localVideoView = nil
        }
        if remoteVideoView == videoView {
            remoteVideoView = nil
        }
    }
    
    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc
    func multiply(_ a: Float, b: Float, resolve:RCTPromiseResolveBlock, reject:RCTPromiseRejectBlock) -> Void {
        resolve(a*b)
    }
    
    @objc
    func getRegions(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        resolve(Boostlingo.getRegions())
    }
    
    @objc
    func getVersion(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        resolve(Boostlingo.getVersion())
    }
    
    @objc
    func initialize(_ config: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        DispatchQueue.main.async {
            do {
                let authToken: String = config["authToken"] as! String
                let region: String = config["region"] as! String
                
                self.boostlingo = Boostlingo(authToken: authToken, region: region, logLevel: .debug)
                self.boostlingo!.initialize() { [weak self] error in
                    guard let self = self else {
                        return
                    }
                    
                    guard error == nil else {
                        let message: String
                        switch error! {
                        case BLError.apiCall(_, let statusCode):
                            message = "\(error!.localizedDescription), statusCode: \(statusCode)"
                            break
                        default:
                            message = error!.localizedDescription
                            break
                        }
                        reject("error", "Encountered an error: \(message)", error)
                        return
                    }
                    
                    resolve(nil)
                }
            } catch let error as NSError {
                reject("error", error.domain, error)
            } catch let error {
                reject("error", "Error running Boostlingo SDK", error)
                return
            }
        }
    }
    
    @objc
    func getCurrentCall(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            let currentCall = self.boostlingo!.currentCall
            resolve(callAsDictionary(call: currentCall))
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func getCallDictionaries(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            self.boostlingo!.getCallDictionaries() { [weak self] (callDictionaries, error) in
                guard let self = self else {
                    return
                }
                
                if error == nil {
                    let result = try! self.callDictionariesAsDictionary(callDictionaries: callDictionaries)
                    resolve(result)
                }
                else {
                    let message: String
                    switch error! {
                    case BLError.apiCall(_, let statusCode):
                        message = "\(error!.localizedDescription), statusCode: \(statusCode)"
                        break
                    default:
                        message = error!.localizedDescription
                        break
                    }
                    reject("error", "Encountered an error: \(message)", error)
                }
            }
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func getProfile(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            self.boostlingo!.getProfile { [weak self] (profile, error) in
                guard let self = self else {
                    return
                }
                
                if error == nil {
                    resolve(self.profileAsDictionary(profile: profile))
                }
                else {
                    let message: String
                    switch error! {
                    case BLError.apiCall(_, let statusCode):
                        message = "\(error!.localizedDescription), statusCode: \(statusCode)"
                        break
                    default:
                        message = error!.localizedDescription
                        break
                    }
                    reject("error", "Encountered an error: \(message)", error)
                }
            }
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func getVoiceLanguages(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            self.boostlingo!.getVoiceLanguages { [weak self] (languages, error) in
                guard let self = self else {
                    return
                }
                
                if error == nil {
                    resolve(languages?.map { l in self.languageAsDictionary(language: l)})
                }
                else {
                    let message: String
                    switch error! {
                    case BLError.apiCall(_, let statusCode):
                        message = "\(error!.localizedDescription), statusCode: \(statusCode)"
                        break
                    default:
                        message = error!.localizedDescription
                        break
                    }
                    reject("error", "Encountered an error: \(message)", error)
                }
            }
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func getVideoLanguages(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            self.boostlingo!.getVideoLanguages { [weak self] (languages, error) in
                guard let self = self else {
                    return
                }
                
                if error == nil {
                    resolve(languages?.map { l in self.languageAsDictionary(language: l)})
                }
                else {
                    let message: String
                    switch error! {
                    case BLError.apiCall(_, let statusCode):
                        message = "\(error!.localizedDescription), statusCode: \(statusCode)"
                        break
                    default:
                        message = error!.localizedDescription
                        break
                    }
                    reject("error", "Encountered an error: \(message)", error)
                }
            }
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func getCallDetails(_ callId: Int, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            self.boostlingo!.getCallDetails(callId: callId) { [weak self] (callDetails, error) in
                guard let self = self else {
                    return
                }
                
                if error == nil {
                    resolve(self.callDetailsAsDictionary(callDetails: callDetails))
                }
                else {
                    let message: String
                    switch error! {
                    case BLError.apiCall(_, let statusCode):
                        message = "\(error!.localizedDescription), statusCode: \(statusCode)"
                        break
                    default:
                        message = error!.localizedDescription
                        break
                    }
                    reject("error", "Encountered an error: \(message)", error)
                }
            }
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func makeVoiceCall(_ request: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            let callRequest = CallRequest(languageFromId: request["languageFromId"] as! Int, languageToId: request["languageToId"] as! Int, serviceTypeId: request["serviceTypeId"] as! Int, genderId: request["genderId"] as? Int, isVideo: false)

            self.boostlingo!.chatDelegate = self
            self.boostlingo!.makeVoiceCall(callRequest: callRequest, delegate: self) { [weak self] call, error in
                guard let self = self else {
                    return
                }
                
                DispatchQueue.main.async {
                    if let error = error {
                        let message = error.localizedDescription
                        reject("error", "Encountered an error: \(message)", error)
                        return
                    }
                    resolve(self.callAsDictionary(call: call))
                }
            }
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func makeVideoCall(_ request: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            let callRequest = CallRequest(languageFromId: request["languageFromId"] as! Int, languageToId: request["languageToId"] as! Int, serviceTypeId: request["serviceTypeId"] as! Int, genderId: request["genderId"] as? Int, isVideo: true)

            self.boostlingo!.chatDelegate = self
            self.boostlingo!.makeVideoCall(callRequest: callRequest, remoteVideoView: remoteVideoView!, localVideoView: localVideoView, delegate: self, videoDelegate: self) { [weak self] call, error in
                guard let self = self else {
                    return
                }
                
                DispatchQueue.main.async {
                    if let error = error {
                        let message = error.localizedDescription
                        reject("error", "Encountered an error: \(message)", error)
                        return
                    }
                    resolve(self.callAsDictionary(call: call))
                }
            }
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func hangUp(_ resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            boostlingo!.hangUp() { [weak self] error in
                guard let self = self else { return }
               
                DispatchQueue.main.async {
                    if let error = error {
                        let message = error.localizedDescription
                       reject("error", "Encountered an error: \(message)", error)
                       return
                    } else {
                         resolve(nil)
                    }
                }
            }
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func toggleAudioRoute(_ toSpeaker: Bool) {
        boostlingo!.toggleAudioRoute(toSpeaker: toSpeaker)
    }
    
    @objc
    func sendChatMessage(_ text: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
        do {
            boostlingo!.sendChatMessage(text: text) { [weak self] (chatMessage, error) in
                guard let self = self else { return }
               
                DispatchQueue.main.async {
                    if let error = error {
                        let message = error.localizedDescription
                        reject("error", "Encountered an error: \(message)", error)
                        return
                    } else {
                        resolve(self.chatMessageAsDictionary(chatMessage: chatMessage))
                    }
                }
            }
        } catch let error as NSError {
            reject("error", error.domain, error)
        } catch let error {
            reject("error", "Error running Boostlingo SDK", error)
            return
        }
    }
    
    @objc
    func muteCall(_ isMuted: Bool) {
        guard let currentCall = boostlingo!.currentCall else {
            return
        }
        currentCall.isMuted = isMuted
    }
    
    @objc
    func enableVideo(_ isVideoEnabled: Bool) {
        guard let currentCall = boostlingo!.currentCall as? BLVideoCall else {
            return
        }
        currentCall.isVideoEnabled = isVideoEnabled
    }
    
    @objc
    func flipCamera() {
        guard let currentCall = boostlingo!.currentCall as? BLVideoCall else {
            return
        }
        currentCall.flipCamera()
    }
    
    @objc
    func dispose() {
        localVideoView = nil
        remoteVideoView = nil
        boostlingo?.chatDelegate = nil
        boostlingo = nil
    }
    
    private func callDictionariesAsDictionary(callDictionaries: CallDictionaries?) -> [String: Any]? {
        guard let callDictionaries = callDictionaries else {
            return nil
        }
        guard let data = try? JSONEncoder().encode(callDictionaries) else {
            return nil
        }
        guard let dictionary = try? JSONSerialization.jsonObject(with: data, options: .allowFragments) as? [String: Any] else {
            return nil
        }
        return dictionary
    }
    
    private func callAsDictionary(call: BLCall?) -> [String: Any]? {
        guard let call = call else {
            return nil
        }
        var dictionary = [String: Any]()
        dictionary["callId"] = call.callId
        dictionary["isVideo"] = call.isVideo
        dictionary["isInProgress"] = call.isInProgress
        dictionary["interlocutorInfo"] = interlocutorInfoAsDictionary(interlocutorInfo: call.interlocutorInfo)
        dictionary["isMuted"] = call.isMuted
        dictionary["accessToken"] = call.accessToken
        dictionary["identity"] = call.identity
        if let videoCall = call as? BLVideoCall {
            dictionary["roomId"] = videoCall.roomId
        } else {
            dictionary["roomId"] = nil
        }
        return dictionary
    }
    
    private func languageAsDictionary(language: Language?) -> [String: Any]? {
        guard let language = language else {
            return nil
        }
        guard let data = try? JSONEncoder().encode(language) else {
            return nil
        }
        guard let dictionary = try? JSONSerialization.jsonObject(with: data, options: .allowFragments) as? [String: Any] else {
            return nil
        }
        return dictionary
    }
    
    private func interlocutorInfoAsDictionary(interlocutorInfo: InterpreterInfo?) -> [String: Any]? {
        guard let interlocutorInfo = interlocutorInfo else {
            return nil
        }
        var dictionary = [String: Any]()
        dictionary["userAccountId"] = interlocutorInfo.userAccountId
        dictionary["firstName"] = interlocutorInfo.firstName
        dictionary["lastName"] = interlocutorInfo.lastName
        dictionary["requiredName"] = interlocutorInfo.requiredName
        dictionary["companyName"] = interlocutorInfo.companyName
        dictionary["rating"] = interlocutorInfo.rating
        dictionary["imageInfo"] = imageInfoAsDictionary(imageInfo: interlocutorInfo.imageInfo)
        return dictionary
    }
    
    private func imageInfoAsDictionary(imageInfo: ImageInfo?) -> [String: Any]? {
        guard let imageInfo = imageInfo else {
            return nil
        }
        var dictionary = [String: Any]()
        dictionary["imageKey"] = imageInfo.imageKey
        dictionary["sizes"] = imageInfo.sizes
        return dictionary
    }
    
    private func profileAsDictionary(profile: Profile?) -> [String: Any]? {
        guard let profile = profile else {
            return nil
        }
        var dictionary = [String: Any]()
        dictionary["accountName"] = profile.accountName
        dictionary["userAccountId"] = profile.userAccountId
        dictionary["companyAccountId"] = profile.companyAccountId
        dictionary["email"] = profile.email
        dictionary["firstName"] = profile.firstName
        dictionary["lastName"] = profile.lastName
        dictionary["requiredName"] = profile.requiredName
        dictionary["imageInfo"] = imageInfoAsDictionary(imageInfo: profile.imageInfo)
        return dictionary
    }
    
    private func callDetailsAsDictionary(callDetails: CallDetails?) -> [String: Any]? {
        guard let callDetails = callDetails else {
            return nil
        }
        var dictionary = [String: Any]()
        dictionary["callId"] = callDetails.callId
        dictionary["accountUniqueId"] = callDetails.accountUniqueId
        dictionary["duration"] = callDetails.duration
        dictionary["timeRequested"] = callDetails.timeRequested.timeIntervalSince1970
        dictionary["timeAnswered"] = callDetails.timeAnswered?.timeIntervalSince1970
        dictionary["timeConnected"] = callDetails.timeConnected?.timeIntervalSince1970
        return dictionary
    }
    
    private func chatMessageAsDictionary(chatMessage: ChatMessage?) -> [String: Any]? {
        guard let chatMessage = chatMessage else {
            return nil
        }
        var dictionary = [String: Any]()
        dictionary["user"] = chatUserAsDictionary(chatUser: chatMessage.user)
        dictionary["text"] = chatMessage.text
        dictionary["sentTime"] = chatMessage.sentTime.timeIntervalSince1970
        return dictionary
    }
    
    private func chatUserAsDictionary(chatUser: ChatUser?) -> [String: Any]? {
        guard let chatUser = chatUser else {
            return nil
        }
        var dictionary = [String: Any]()
        dictionary["id"] = chatUser.id
        dictionary["imageInfo"] = imageInfoAsDictionary(imageInfo: chatUser.imageInfo)
        return dictionary
    }
    
    // MARK: - BLCallDelegate
    func callDidConnect(_ call: BLCall) {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "callDidConnect", body: self.callAsDictionary(call: call))
            }
        }
    }
    
    func callDidDisconnect(_ error: Error?) {
        if (hasListeners) {
            DispatchQueue.main.async {
                 self.sendEvent(withName: "callDidDisconnect", body: error != nil ? error!.localizedDescription : nil)
            }
        }
    }
    
    func callDidFailToConnect(_ error: Error?) {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "callDidFailToConnect", body: error != nil ? error!.localizedDescription : nil)
            }
        }
    }
    
    // MARK: - BLChatDelegate
    func chatConnected() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "chatConnected", body: nil)
            }
        }
    }
    
    func chatDisconnected() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "chatDisconnected", body: nil)
            }
        }
    }
    
    func chatMessageRecieved(message: ChatMessage) {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "chatMessageRecieved", body: self.chatMessageAsDictionary(chatMessage: message))
            }
        }
    }
    
    // MARK: - BLVideoDelegate
    func remoteAudioEnabled() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "remoteAudioEnabled", body: nil)
            }
        }
    }
    
    func remoteAudioDisabled() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "remoteAudioDisabled", body: nil)
            }
        }
    }
    
    func remoteVideoEnabled() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "remoteVideoEnabled", body: nil)
            }
        }
    }
    
    func remoteVideoDisabled() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "remoteVideoDisabled", body: nil)
            }
        }
    }
    
    func remoteAudioPublished() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "remoteAudioPublished", body: nil)
            }
        }
    }
    
    func remoteAudioUnpublished() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "remoteAudioUnpublished", body: nil)
            }
        }
    }
    
    func remoteVideoPublished() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "remoteVideoPublished", body: nil)
            }
        }
    }
    
    func remoteVideoUnpublished() {
        if (hasListeners) {
            DispatchQueue.main.async {
                self.sendEvent(withName: "remoteVideoUnpublished", body: nil)
            }
        }
    }
}
