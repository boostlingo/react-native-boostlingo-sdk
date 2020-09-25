package com.reactnativeboostlingosdk

import com.boostlingo.android.Boostlingo
import com.facebook.react.bridge.*

class BoostlingoSdkModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

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
}
