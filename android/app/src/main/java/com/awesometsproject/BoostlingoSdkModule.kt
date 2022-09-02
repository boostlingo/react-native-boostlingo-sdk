package com.awesometsproject

import android.media.AudioManager
import com.boostlingo.android.*
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioSwitch
import com.twilio.video.VideoView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException
import kotlin.properties.Delegates

class BoostlingoSdkModule(
  reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

  private var compositeDisposable = CompositeDisposable()

  private var audioSwitch: AudioSwitch? = null

  private var savedVolumeControlStream: Int? = null

  private var boostlingo: BoostlingoSDK? = null
  private var localVideoView: VideoView? = null

  override fun getName(): String {
    return "BoostlingoSdk"
  }

  fun setLocalVideo(videoView: VideoView?) {
    localVideoView = videoView
  }

  fun setRemoteVideo(identity: String, videoView: VideoView?) {
    videoView?.let { videoView ->
      boostlingo?.getCurrentCall()
        ?.let {
          if (it is BLVideoCall) {
            it.addRenderer(identity, videoView)
          }
        }
    }
  }

  @ReactMethod
  fun getRegions(promise: Promise) {
    val result = WritableNativeArray()
    BoostlingoSDK.getRegions()
      .map { region -> result.pushString(region) }
    promise.resolve(result)
  }

  @ReactMethod
  fun getVersion(promise: Promise) {
    promise.resolve(BoostlingoSDK.VERSION)
  }

  @ReactMethod
  fun initialize(config: ReadableMap, promise: Promise) {
    try {
      boostlingo = BoostlingoSDK(
        config.getString("authToken")!!,
        reactApplicationContext,
        BLLogLevel.DEBUG,
        config.getString("region")!!)

      audioSwitch = AudioSwitch(
        reactApplicationContext,
        preferredDeviceList = listOf(
          AudioDevice.BluetoothHeadset::class.java,
          AudioDevice.WiredHeadset::class.java,
          AudioDevice.Speakerphone::class.java,
          AudioDevice.Earpiece::class.java
        )
      )

      boostlingo!!.callEventObservable
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          ::handleCallEvent,
          ::handleError
        )
        .let { compositeDisposable.add(it) }

      boostlingo!!.initialize()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            promise.resolve(null)
          },
          {
            promise.reject(cause(it).localizedMessage, it)
          }
        )
        .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  private fun cause(t: Throwable): Throwable {
    return t.cause
      ?.let {
        cause(it)
      } ?: t
  }

  private fun handleCallEvent(callEvent: BLCallEvent) {
    when (callEvent) {
      is BLCallEvent.CallConnected -> {
        audioSwitch?.activate()
        reactApplicationContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("callDidConnect", mapCall(callEvent.call))
      }
      is BLCallEvent.CallDisconnected -> {
        audioSwitch?.deactivate()
        reactApplicationContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("callDidDisconnect", callEvent.e?.localizedMessage)
      }
      is BLCallEvent.CallFailedToConnect -> {
        audioSwitch?.deactivate()
        reactApplicationContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("callDidFailToConnect", callEvent.e?.localizedMessage)
      }
      BLCallEvent.ChatConnected -> {
        reactApplicationContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("chatConnected", null)
      }
      BLCallEvent.ChatDisconnected -> {
        reactApplicationContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("chatDisconnected", null)
      }
      is BLCallEvent.ChatMessageReceived -> {
        reactApplicationContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("chatMessageRecieved", mapChatMessage(callEvent.message))
      }
      is BLCallEvent.ParticipantAdded -> {
        reactApplicationContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("callParticipantConnected", mapParticipant(callEvent.participant))
      }
      is BLCallEvent.ParticipantRemoved -> {
        reactApplicationContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("callParticipantDisconnected", mapParticipant(callEvent.participant))
      }
      is BLCallEvent.ParticipantUpdated -> {
        reactApplicationContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
          .emit("callParticipantUpdated", mapParticipant(callEvent.participant))
      }
    }
  }

  private fun handleError(t: Throwable) {
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit("error", cause(t).localizedMessage)
  }

  private fun startAudioSwitch() {
    audioSwitch?.start { _: List<AudioDevice?>?, _: AudioDevice? ->
      return@start Unit
    }
  }

  @ReactMethod
  fun getCurrentCall(promise: Promise) {
    try {
      val currentCall = boostlingo!!.getCurrentCall()
      promise.resolve(mapCall(currentCall))
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  private fun mapCall(call: BLCall?): ReadableMap? {
    call?.let {
      val videoCall = it as? BLVideoCall
      with(it) {
        val map = WritableNativeMap()

        callId?.let {
          map.putDouble("callId", it.toDouble())
        } ?: map.putNull("callId")
        map.putDouble("currentUserId", currentUserId.toDouble())
        map.putBoolean("isVideo", isVideo)
        map.putBoolean("isInProgress", isInProgress)
        map.putMap("interlocutorInfo", mapParticipant(interlocutorInfo))
        map.putBoolean("isMuted", isMuted)
        map.putString("accessToken", accessToken)
        map.putString("identity", identity)
        map.putArray("participants", mapParticipants(participants))
        map.putBoolean("canAddThirdParty", canAddThirdParty)

        if (videoCall != null) {
          map.putBoolean("isVideoEnabled", videoCall.isVideoEnabled)
          map.putString("roomId", videoCall.roomId)
        } else {
          map.putNull("isVideoEnabled")
          map.putNull("roomId")
        }

        return map
      }
    }

    return null
  }

  private fun mapParticipant(participant: BLParticipant?): ReadableMap? {
    participant?.let {
      with(it) {
        val map = WritableNativeMap()

        when (participantType) {
          BLParticipantType.CLIENT,
          BLParticipantType.INTERPRETER,
          BLParticipantType.NONE -> {
            map.putDouble("userAccountId", accountId.toDouble())
            map.putNull("thirdPartyParticipantId")
          }
          BLParticipantType.THIRD_PARTY -> {
            map.putNull("userAccountId")
            map.putDouble("thirdPartyParticipantId", accountId.toDouble())
          }
        }
        map.putString("identity", identity)
        map.putInt("participantType", mapParticipantType(participantType))
        map.putMap("imageInfo", mapImageInfo(imageInfo))
        map.putString("requiredName", requiredName)
        rating?.let {
          map.putDouble("rating", it)
        } ?: map.putNull("rating")
        map.putString("companyName", companyName)
        map.putInt("state", mapState(state))
        map.putBoolean("isAudioEnabled", isAudioEnabled)
        map.putBoolean("isVideoEnabled", isVideoEnabled)
        map.putBoolean("muteActionIsEnabled", muteActionIsEnabled)
        map.putBoolean("removeActionIsEnabled", removeActionIsEnabled)

        return map
      }
    }

    return null
  }

  private fun mapParticipantType(participantType: BLParticipantType): Int {
    return when (participantType) {
      BLParticipantType.CLIENT -> 1
      BLParticipantType.INTERPRETER -> 2
      BLParticipantType.THIRD_PARTY -> 3
      BLParticipantType.NONE -> 0
    }
  }

  private fun mapState(state: BLParticipantState): Int {
    return when (state) {
      BLParticipantState.CONFIRMATION -> 1
      BLParticipantState.CONNECTING -> 2
      BLParticipantState.CONNECTED -> 3
      BLParticipantState.DISCONNECTED -> 4
      BLParticipantState.NONE -> 0
    }
  }

  private fun mapImageInfo(imageInfo: ImageInfo?): ReadableMap? {
    imageInfo?.let {
      with(it) {
        val map = WritableNativeMap()

        map.putString("imageKey", imageKey)
        val sizesArray = WritableNativeArray()
        sizes.map { size -> sizesArray.pushInt(size) }
        map.putArray("sizes", sizesArray)
        map.putString("baseUrl", baseUrl)
        map.putString("fileExtension", extension)

        return map
      }
    }

    return null
  }

  private fun mapParticipants(participants: List<BLParticipant>): ReadableArray {
    val array = WritableNativeArray()

    for (participant in participants) {
      array.pushMap(mapParticipant(participant))
    }

    return array
  }

  @ReactMethod
  fun getCallDictionaries(promise: Promise) {
    try {
        boostlingo!!.getCallDictionaries()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            {
              promise.resolve(mapCallDictionaries(it))
            },
            {
              promise.reject(cause(it).localizedMessage, it)
            }
          )
          .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  private fun mapCallDictionaries(callDictionaries: CallDictionaries?): ReadableMap? {
    callDictionaries?.let {
      with(it) {
        val map = WritableNativeMap()

        val languagesArray = WritableNativeArray()
        for (language in languages) {
          languagesArray.pushMap(mapLanguage(language))
        }
        map.putArray("languages", languagesArray)

        val serviceTypesArray = WritableNativeArray()
        for (serviceType in serviceTypes) {
          serviceTypesArray.pushMap(mapServiceType(serviceType))
        }
        map.putArray("serviceTypes", serviceTypesArray)

        val gendersArray = WritableNativeArray()
        for (gender in genders) {
          gendersArray.pushMap(mapGender(gender))
        }
        map.putArray("genders", gendersArray)

        return map
      }
    }

    return null
  }

  private fun mapLanguage(language: Language?): ReadableMap? {
    language?.let {
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
          map.putInt("vriPolicyOrder", vriPolicyOrder ?: 0)
        } else  {
          map.putNull("vriPolicyOrder")
        }
        if (opiPolicyOrder != null) {
          map.putInt("opiPolicyOrder", opiPolicyOrder ?: 0)
        } else {
          map.putNull("opiPolicyOrder")
        }

        return map
      }
    }

    return null
  }

  private fun mapLanguages(languages: List<Language>?): ReadableArray? {
    return languages?.let {
      val array = WritableNativeArray()

      for (language in it) {
        array.pushMap(mapLanguage(language))
      }

      return array
    }
  }

  private fun mapServiceType(serviceType: ServiceType?): ReadableMap? {
   serviceType?.let {
      with(it) {
        val map = WritableNativeMap()

        map.putInt("id", id)
        map.putString("name", name)
        map.putBoolean("enable", enable)

        return map
      }
    }

    return null
  }

  private fun mapGender(gender: Gender?): ReadableMap? {
   gender?.let {
      with(it) {
        val map = WritableNativeMap()

        map.putInt("id", id.toInt())
        map.putString("name", name)

        return map
      }
    }

    return null
  }

  @ReactMethod
  fun getProfile(promise: Promise) {
    try {
      boostlingo!!.getProfile()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            promise.resolve(mapProfile(it))
          },
          {
            promise.reject(cause(it).localizedMessage, it)
          }
        )
        .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  private fun mapProfile(profile: Profile?): ReadableMap? {
    profile?.let {
      with(it) {
        val map = WritableNativeMap()

        map.putString("accountName", accountName)
        map.putDouble("userAccountId", userAccountId.toDouble())
        map.putDouble("companyAccountId", companyAccountId.toDouble())
        map.putString("email", email)
        map.putString("firstName", firstName)
        map.putString("lastName", lastName)
        map.putMap("imageInfo", mapImageInfo(imageInfo))

        return map
      }
    }

    return null
  }

  @ReactMethod
  fun getVoiceLanguages(promise: Promise) {
    try {
        boostlingo!!.getVoiceLanguages()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            {
              promise.resolve(mapLanguages(it))
            },
            {
              promise.reject(cause(it).localizedMessage, it)
            }
          )
          .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  @ReactMethod
  fun getVideoLanguages(promise: Promise) {
    try {
        boostlingo!!.getVideoLanguages()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            {
              promise.resolve(mapLanguages(it))
            },
            {
              promise.reject(cause(it).localizedMessage, it)
            }
          )
          .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  @ReactMethod
  fun getCallDetails(callId: Double, promise: Promise) {
    try {
        boostlingo!!.getCallDetails(callId.toLong())
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
            {
              promise.resolve(mapCallDetails(it))
            },
            {
              promise.reject(cause(it).localizedMessage, it)
            }
          )
          .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  private fun mapCallDetails(callDetails: CallDetails?): ReadableMap? {
    callDetails?.let {
      with(it) {
        val map = WritableNativeMap()

        map.putDouble("callId", callId.toDouble())
        map.putDouble("accountUniqueId", accountUniqueId.toDouble())
        map.putDouble("duration", duration)
        map.putDouble("timeRequested", timeRequested.time.toDouble())
        timeAnswered?.let {
          map.putDouble("timeAnswered", it.time.toDouble())
        } ?: map.putNull("timeAnswered")
        timeConnected?.let {
          map.putDouble("timeConnected", it.time.toDouble())
        } ?: map.putNull("timeConnected")

        return map
      }
    }

    return null
  }

  @ReactMethod
  fun makeVoiceCall(request: ReadableMap, promise: Promise) {
    try {
      val calRequest = CallRequest(
        languageFromId = request.getInt("languageFromId"),
        languageToId = request.getInt("languageToId"),
        serviceTypeId = request.getInt("serviceTypeId"),
        genderId = if (request.hasKey("genderId") && !request.isNull("genderId")) request.getInt("genderId") else null,
        isVideo = false,
        data = if (request.hasKey("data")) mapAdditionalCallData(request.getArray("data")) else null
      )

      reactApplicationContext.currentActivity
        ?.let {
          savedVolumeControlStream = it.volumeControlStream
          it.volumeControlStream = AudioManager.STREAM_VOICE_CALL
        }

      startAudioSwitch()

      boostlingo!!.makeVoiceCall(calRequest)
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            promise.resolve(mapCall(it))
          },
          {
            promise.reject(cause(it).localizedMessage, it)
          }
        )
        .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  private fun mapAdditionalCallData(data: ReadableArray?): List<AdditionalField>? {
    return data?.let {
      it.toArrayList()
        .map {
          AdditionalField(
            key = (it as HashMap<String, String>)["key"]!!,
            value = (it as HashMap<String, String>)["value"]!!
          )
        }
    }
  }

  @ReactMethod
  fun makeVideoCall(request: ReadableMap, promise: Promise) {
    try {
      val calRequest = CallRequest(
        languageFromId = request.getInt("languageFromId"),
        languageToId = request.getInt("languageToId"),
        serviceTypeId = request.getInt("serviceTypeId"),
        genderId = if (request.hasKey("genderId") && !request.isNull("genderId")) request.getInt("genderId") else null,
        isVideo = true,
        data = if (request.hasKey("data")) mapAdditionalCallData(request.getArray("data")) else null
      )

      boostlingo!!.makeVideoCall(calRequest, localVideoView)
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            promise.resolve(mapCall(it))
          },
          {
            promise.reject(cause(it).localizedMessage, it)
          }
        )
        .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  @ReactMethod
  fun hangUp(promise: Promise) {
    try {
      boostlingo!!.hangUp()
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            promise.resolve(null)
          },
          {
            promise.reject(cause(it).localizedMessage, it)
          }
        )
        .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  @ReactMethod
  fun toggleAudioRoute(toSpeaker: Boolean) {
    if (toSpeaker) {
      audioSwitch?.selectDevice(
        audioSwitch?.availableAudioDevices?.firstOrNull {
          it is AudioDevice.Speakerphone
        }
      )
    } else {
      audioSwitch?.selectDevice(
          audioSwitch?.availableAudioDevices?.firstOrNull {
            it is AudioDevice.Earpiece
          }
      )
    }
  }

  @ReactMethod
  fun sendChatMessage(text: String, promise: Promise) {
    try {
      boostlingo!!.sendChatMessage(text)
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            promise.resolve(mapChatMessage(it))
          },
          {
            promise.reject(cause(it).localizedMessage, it)
          }
        )
        .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  private fun mapChatMessage(chatMessage: ChatMessage?): ReadableMap? {
    chatMessage?.let {
      with(it) {
        val map = WritableNativeMap()

        map.putMap("user", mapChatUser(user))
        map.putString("text", text)
        map.putDouble("sentTime", sentTime.time.toDouble())

        return map
      }
    }

    return null
  }

  private fun mapChatUser(chatUser: ChatUser?): ReadableMap? {
    chatUser?.let {
      with(it) {
        val map = WritableNativeMap()

        map.putDouble("id", id.toDouble())
        map.putMap("imageInfo", mapImageInfo(imageInfo))

        return map
      }
    }

    return null
  }

  @ReactMethod
  fun muteCall(isMuted: Boolean) {
    boostlingo?.getCurrentCall()?.isMuted = isMuted
  }

  @ReactMethod
  fun enableVideo(isVideoEnabled: Boolean) {
    val videoCall = boostlingo?.getCurrentCall() as? BLVideoCall
    videoCall?.isVideoEnabled = isVideoEnabled
  }

  @ReactMethod
  fun flipCamera() {
    val videoCall = boostlingo?.getCurrentCall() as? BLVideoCall
    videoCall?.switchCameraSource()
  }

  @ReactMethod
  fun dialThirdParty(phone: String, promise: Promise) {
    try {
      val call = boostlingo!!.getCurrentCall()
      if (call == null) {
        promise.resolve(null)
        return
      }

      call.dialThirdParty(phone)
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            promise.resolve(null)
          },
          {
            promise.reject(cause(it).localizedMessage, it)
          }
        )
        .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  @ReactMethod
  fun hangUpThirdParty(identity: String, promise: Promise) {
    try {
      val call = boostlingo!!.getCurrentCall()
      if (call == null) {
        promise.resolve(null)
        return
      }

      call.hangupThirdPartyParticipant(identity)
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            promise.resolve(null)
          },
          {
            promise.reject(cause(it).localizedMessage, it)
          }
        )
        .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  @ReactMethod
  fun muteThirdParty(request: ReadableArray, promise: Promise) {
    try {
      val call = boostlingo!!.getCurrentCall()
      if (call == null) {
        promise.resolve(null)
        return
      }

      val identity = request.toArrayList()[0] as? String
      if (identity == null) {
        promise.resolve(null)
        return
      }

      val mute = request.toArrayList()[1] as? Boolean
      if (mute == null) {
        promise.resolve(null)
        return
      }

      call.muteThirdPartyParticipant(identity, mute)
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
          {
            promise.resolve(null)
          },
          {
            promise.reject(cause(it).localizedMessage, it)
          }
        )
        .let { compositeDisposable.add(it) }
    } catch (e: Exception) {
      promise.reject(cause(e).localizedMessage, e)
    }
  }

  @ReactMethod
  fun dispose() {
    try {
      compositeDisposable.dispose()
      compositeDisposable = CompositeDisposable()
      localVideoView = null
      boostlingo?.dispose()
      boostlingo = null
      audioSwitch?.stop()
      audioSwitch = null

      reactApplicationContext.currentActivity
        ?.let { activity ->
          savedVolumeControlStream?.let {
            activity.volumeControlStream = it
          }
        }
    } catch (e: Exception) {
      reactApplicationContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
        .emit("error", cause(e).localizedMessage)
    }
  }
}
