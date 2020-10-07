package com.reactnativeboostlingosdk

import android.graphics.Color
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import java.lang.Exception

/**
 * @author Denis Kornev
 */
class BLVideoViewManager(
    private val reactContext: ReactApplicationContext
) : SimpleViewManager<RNVideoViewGroup>() {

    override fun getName(): String {
        return "BLVideoView"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): RNVideoViewGroup {
        return RNVideoViewGroup(reactContext)
    }

    override fun getCommandsMap(): MutableMap<String, Int> {
        return mutableMapOf(
                "attachAsLocal" to 1,
                "attachAsRemote" to 2,
                "detach" to 3
        )
    }

    override fun receiveCommand(root: RNVideoViewGroup, commandId: Int, args: ReadableArray?) {
        val boostlingoSdkModule  = reactContext.catalystInstance.getNativeModule("BoostlingoSdk") as BoostlingoSdkModule
        when(commandId) {
            1 -> boostlingoSdkModule.setLocalVideo(root.getSurfaceViewRenderer())
            2 -> boostlingoSdkModule.setRemoteVideo(root.getSurfaceViewRenderer())
            3 -> boostlingoSdkModule.detachVideoView(root.getSurfaceViewRenderer())
        }
    }

    override fun receiveCommand(root: RNVideoViewGroup, commandId: String?, args: ReadableArray?) {
        val boostlingoSdkModule  = reactContext.catalystInstance.getNativeModule("BoostlingoSdk") as BoostlingoSdkModule
        when(commandId) {
            "1" -> boostlingoSdkModule.setLocalVideo(root.getSurfaceViewRenderer())
            "2" -> boostlingoSdkModule.setRemoteVideo(root.getSurfaceViewRenderer())
            "3" -> boostlingoSdkModule.detachVideoView(root.getSurfaceViewRenderer())
        }
    }
}