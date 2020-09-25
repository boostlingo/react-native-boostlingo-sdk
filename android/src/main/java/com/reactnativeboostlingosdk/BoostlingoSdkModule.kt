package com.reactnativeboostlingosdk

import android.R
import android.widget.ArrayAdapter
import com.boostlingo.android.*
import com.facebook.react.bridge.*
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import io.reactivex.CompletableObserver
import io.reactivex.CompletableSource
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class BoostlingoSdkModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

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
                    val apiCallException = e as BLApiCallException?
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
        boostlingo = null
    }
}
