package com.reactnativeboostlingosdk

import com.boostlingo.android.*
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import io.reactivex.CompletableObserver
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class BoostlingoSdkModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext), BLCallStateListener, BLChatListener {

    private var compositeDisposable = CompositeDisposable()
    private var boostlingo: Boostlingo? = null

    override fun getName(): String {
        return "BoostlingoSdk"
    }

    // Example method
    // See https://facebook.github.io/react-native/docs/native-modules-android
    @ReactMethod
    fun multiply(a: Int, b: Int, promise: Promise) {
        promise.resolve(a * b)
    }

    @ReactMethod
    fun getRegions(promise: Promise) {
        val result = WritableNativeArray()
        Boostlingo.getRegions().map { region -> result.pushString(region) }
        promise.resolve(result)
    }

    @ReactMethod
    fun getVersion(promise: Promise) {
        promise.resolve(Boostlingo.getVersion())
    }

    @ReactMethod
    fun initialize(config: ReadableMap, promise: Promise) {
        try {
            boostlingo = Boostlingo(reactApplicationContext, config.getString("authToken")!!, config.getString("region")!!, BLLogLevel.DEBUG)

            boostlingo!!.initialize().subscribe(object: CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.addAll(d)
                }

                override fun onComplete() {
                    promise.resolve(null)
                }

                override fun onError(e: Throwable) {
                    val apiCallException = e as? BLApiCallException?
                    var message = ""
                    if (apiCallException != null) {
                        message = "${apiCallException.localizedMessage}, statusCode: ${apiCallException.statusCode}"
                    } else {
                        message = e.localizedMessage
                    }
                    promise.reject("error", Exception(message, e))
                }
            })
        } catch (e: Exception) {
            promise.reject("error", Exception("Error running Boostlingo SDK", e))
        }
    }

    @ReactMethod
    fun getCurrentCall(promise: Promise) {
        try {
            val currentCall = boostlingo!!.getCurrentCall()
            promise.resolve(mapCall(currentCall))
        } catch (e: Exception) {
            promise.reject("error", Exception("Error running Boostlingo SDK", e))
        }
    }

    @ReactMethod
    fun getCallDictionaries(promise: Promise) {
        try {
            boostlingo!!.callDictionaries.subscribe(object: SingleObserver<CallDictionaries?> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.addAll(d)
                }

                override fun onSuccess(t: CallDictionaries) {
                    promise.resolve(mapCallDictionaries(t))
                }

                override fun onError(e: Throwable) {
                    val apiCallException = e as? BLApiCallException?
                    var message = ""
                    if (apiCallException != null) {
                        message = "${apiCallException.localizedMessage}, statusCode: ${apiCallException.statusCode}"
                    } else {
                        message = e.localizedMessage
                    }
                    promise.reject("error", Exception(message, e))
                }
            })
        } catch (e: Exception) {
            promise.reject("error", Exception("Error running Boostlingo SDK", e))
        }
    }

    @ReactMethod
    fun getProfile(promise: Promise) {
        try {
            boostlingo!!.profile.subscribe(object: SingleObserver<Profile?> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.addAll(d)
                }

                override fun onSuccess(t: Profile) {
                    promise.resolve(mapProfile(t))
                }

                override fun onError(e: Throwable) {
                    val apiCallException = e as? BLApiCallException?
                    var message = ""
                    if (apiCallException != null) {
                        message = "${apiCallException.localizedMessage}, statusCode: ${apiCallException.statusCode}"
                    } else {
                        message = e.localizedMessage
                    }
                    promise.reject("error", Exception(message, e))
                }
            })
        } catch (e: Exception) {
            promise.reject("error", Exception("Error running Boostlingo SDK", e))
        }
    }

    @ReactMethod
    fun getVoiceLanguages(promise: Promise) {
        try {
            boostlingo!!.voiceLanguages.subscribe(object: SingleObserver<List<Language>?> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.addAll(d)
                }

                override fun onSuccess(t: List<Language>) {
                    promise.resolve(mapLanguages(t))
                }

                override fun onError(e: Throwable) {
                    val apiCallException = e as? BLApiCallException?
                    var message = ""
                    if (apiCallException != null) {
                        message = "${apiCallException.localizedMessage}, statusCode: ${apiCallException.statusCode}"
                    } else {
                        message = e.localizedMessage
                    }
                    promise.reject("error", Exception(message, e))
                }
            })
        } catch (e: Exception) {
            promise.reject("error", Exception("Error running Boostlingo SDK", e))
        }
    }

    @ReactMethod
    fun getVideoLanguages(promise: Promise) {
        try {
            boostlingo!!.videoLanguages.subscribe(object: SingleObserver<List<Language>?> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.addAll(d)
                }

                override fun onSuccess(t: List<Language>) {
                    promise.resolve(mapLanguages(t))
                }

                override fun onError(e: Throwable) {
                    val apiCallException = e as? BLApiCallException?
                    var message = ""
                    if (apiCallException != null) {
                        message = "${apiCallException.localizedMessage}, statusCode: ${apiCallException.statusCode}"
                    } else {
                        message = e.localizedMessage
                    }
                    promise.reject("error", Exception(message, e))
                }
            })
        } catch (e: Exception) {
            promise.reject("error", Exception("Error running Boostlingo SDK", e))
        }
    }

    @ReactMethod
    fun getCallDetails(callId: Int, promise: Promise) {
        try {
            boostlingo!!.getCallDetails(callId).subscribe(object: SingleObserver<CallDetails?> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.addAll(d)
                }

                override fun onSuccess(t: CallDetails) {
                    promise.resolve(mapCallDetails(t))
                }

                override fun onError(e: Throwable) {
                    val apiCallException = e as? BLApiCallException?
                    var message = ""
                    if (apiCallException != null) {
                        message = "${apiCallException.localizedMessage}, statusCode: ${apiCallException.statusCode}"
                    } else {
                        message = e.localizedMessage
                    }
                    promise.reject("error", Exception(message, e))
                }
            })
        } catch (e: Exception) {
            promise.reject("error", Exception("Error running Boostlingo SDK", e))
        }
    }

    @ReactMethod
    fun makeVoiceCall(request: ReadableMap, promise: Promise) {
        try {
            val calRequest = CallRequest(
                    request.getInt("languageFromId"),
                    request.getInt("languageToId"),
                    request.getInt("serviceTypeId"),
                    if (request.hasKey("genderId") && !request.isNull("genderId")) request.getInt("genderId") else null )
            boostlingo!!.makeVoiceCall(calRequest, this, this).subscribe(object: SingleObserver<BLVoiceCall?> {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.addAll(d)
                }

                override fun onSuccess(t: BLVoiceCall) {
                    promise.resolve(mapCall(t))
                }

                override fun onError(e: Throwable) {
                    val apiCallException = e as? BLApiCallException?
                    var message = ""
                    if (apiCallException != null) {
                        message = "${apiCallException.localizedMessage}, statusCode: ${apiCallException.statusCode}"
                    } else {
                        message = e.localizedMessage
                    }
                    promise.reject("error", Exception(message, e))
                }
            })
        } catch (e: Exception) {
            promise.reject("error", Exception("Error running Boostlingo SDK", e))
        }
    }

    @ReactMethod
    fun hangUp(promise: Promise) {
        try {
            boostlingo!!.hangUp().subscribe(object: CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    compositeDisposable.addAll(d)
                }

                override fun onComplete() {
                    promise.resolve(null)
                }

                override fun onError(e: Throwable) {
                    val apiCallException = e as? BLApiCallException?
                    var message = ""
                    if (apiCallException != null) {
                        message = "${apiCallException.localizedMessage}, statusCode: ${apiCallException.statusCode}"
                    } else {
                        message = e.localizedMessage
                    }
                    promise.reject("error", Exception(message, e))
                }
            })
        } catch (e: Exception) {
            promise.reject("error", Exception("Error running Boostlingo SDK", e))
        }
    }

    @ReactMethod
    fun dispose() {
        compositeDisposable.dispose()
        compositeDisposable = CompositeDisposable()
        boostlingo?.setCallStateListener(null)
        boostlingo?.setBlChatListener(null)
        boostlingo?.setVideoListener(null)
        boostlingo = null
    }

    private fun mapCall(call: BLCall?): ReadableMap? {
        return call?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putInt("callId", callId)
                map.putBoolean("isVideo", isVideo)
                map.putBoolean("isInProgress", isInProgress)
                map.putMap("interlocutorInfo", mapInterlocutorInfo(interlocutorInfo))
                map.putBoolean("isMuted", isMuted)
                return map
            }
        }
    }

    private fun mapInterlocutorInfo(interlocutorInfo: InterpreterInfo?): ReadableMap? {
        return interlocutorInfo?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putInt("userAccountId", userAccountId)
                map.putString("firstName", firstName)
                map.putString("lastName", lastName)
                map.putString("requiredName", requiredName)
                map.putString("companyName", companyName)
                map.putDouble("rating", rating)
                map.putMap("imageInfo", mapImageInfo(imageInfo))
                return map
            }
        }
    }

    private fun mapImageInfo(imageInfo: ImageInfo?): ReadableMap? {
        return imageInfo?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putString("imageKey", imageKey)
                val sizesArray = WritableNativeArray()
                sizes?.map { size -> sizesArray.pushInt(size) }
                map.putArray("sizes", sizesArray)
                return map
            }
        }
    }

    private fun mapCallDictionaries(callDictionaries: CallDictionaries?): ReadableMap? {
        return callDictionaries?.let {
            with(it) {
                val map = WritableNativeMap()
                val languagesArray = WritableNativeArray()
                languages?.map { language -> languagesArray.pushMap(mapLanguage(language)) }
                map.putArray("languages", languagesArray)
                val serviceTypesArray = WritableNativeArray()
                serviceTypes?.map { serviceType -> serviceTypesArray.pushMap(mapServiceType(serviceType)) }
                map.putArray("serviceTypes", serviceTypesArray)
                val gendersArray = WritableNativeArray()
                genders?.map { gender -> gendersArray.pushMap(mapGender(gender)) }
                map.putArray("genders", gendersArray)
                return map
            }
        }
    }

    private fun mapLanguage(language: Language?): ReadableMap? {
        return language?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putInt("id", id)
                map.putString("code", code)
                map.putString("name", name)
                map.putString("englishName", englishName)
                map.putString("nativeName", nativeName)
                map.putString("localizedName", localizedName)
                map.putBoolean("enabled", enabled)
                map.putBoolean("isSignLanguage", isSignLanguage)
                map.putBoolean("isVideoBackstopStaffed", isVideoBackstopStaffed)
                if (vriPolicyOrder != null) {
                    map.putInt("vriPolicyOrder", vriPolicyOrder)
                } else  {
                    map.putNull("vriPolicyOrder")
                }
                if (opiPolicyOrder != null) {
                    map.putInt("opiPolicyOrder", opiPolicyOrder)
                } else {
                    map.putNull("opiPolicyOrder")
                }
                return map
            }
        }
    }

    private fun mapLanguages(languages: List<Language>?): ReadableArray? {
        return languages?.let {
            val array = WritableNativeArray()
            it.map { language -> array.pushMap(mapLanguage(language)) }
            return array
        }
    }

    private fun mapServiceType(serviceType: ServiceType?): ReadableMap? {
        return serviceType?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putInt("id", id)
                map.putString("name", name)
                map.putBoolean("enable", enable)
                return map
            }
        }
    }

    private fun mapGender(gender: Gender?): ReadableMap? {
        return gender?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putInt("id", id)
                map.putString("name", name)
                return map
            }
        }
    }

    private fun mapProfile(profile: Profile?): ReadableMap? {
        return profile?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putString("accountName", accountName)
                map.putInt("userAccountId", userAccountId)
                if (companyAccountId != null) {
                    map.putInt("companyAccountId", companyAccountId)
                } else  {
                    map.putNull("companyAccountId")
                }
                map.putString("email", email)
                map.putString("firstName", firstName)
                map.putString("lastName", lastName)
                map.putMap("imageInfo", mapImageInfo(imageInfo))
                return map
            }
        }
    }

    private fun mapCallDetails(callDetails: CallDetails?): ReadableMap? {
        return callDetails?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putInt("callId", callId)
                map.putDouble("accountUniqueId", accountUniqueId.toDouble())
                map.putDouble("duration", duration)
                if (timeRequested != null) {
                    map.putString("timeRequested", timeRequested.toString())
                } else {
                    map.putNull("timeRequested")
                }
                if (timeAnswered != null) {
                    map.putString("timeAnswered", timeAnswered.toString())
                } else {
                    map.putNull("timeAnswered")
                }
                if (timeConnected != null) {
                    map.putString("timeConnected", timeConnected.toString())
                } else {
                    map.putNull("timeConnected")
                }
                return map
            }
        }
    }

    private fun mapChatMessage(chatMessage: ChatMessage?): ReadableMap? {
        return chatMessage?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putMap("user", mapChatUser(user))
                map.putString("text", text)
                map.putString("sentTime", sentTime.toString())
                return map
            }
        }
    }

    private fun mapChatUser(chatUser: ChatUser?): ReadableMap? {
        return chatUser?.let {
            with(it) {
                val map = WritableNativeMap()
                map.putInt("id", id)
                map.putMap("imageInfo", mapImageInfo(imageInfo))
                return map
            }
        }
    }

    // MARK: - BLCallDelegate
    override fun callConnected(p0: BLCall) {
        reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("callConnected", mapCall(p0))
    }

    override fun callFailedToConnect(p0: Throwable?) {
        reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("callDidFailToConnect", p0?.localizedMessage)
    }

    override fun callDisconnected(p0: Throwable?) {
        reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("callDidDisconnect", p0?.localizedMessage)
    }

    // MARK: - BLChatDelegate
    override fun chatConnected() {
        reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("chatConnected", null)
    }

    override fun chatDisconnected() {
        reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("chatDisconnected", null)
    }

    override fun chatMessageReceived(p0: ChatMessage?) {
        reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("chatMessageRecieved", mapChatMessage(p0))
    }
}
