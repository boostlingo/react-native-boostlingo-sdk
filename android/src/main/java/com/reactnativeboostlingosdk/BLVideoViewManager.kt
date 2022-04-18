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
) : SimpleViewManager<BLVideoViewGroup>() {

    override fun getName(): String {
        return "BLVideoView"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): BLVideoViewGroup {
        return BLVideoViewGroup(reactContext)
    }

    override fun getCommandsMap(): MutableMap<String, Int> {
        return mutableMapOf(
                "attachAsLocal" to 1,
                "attachAsRemote" to 2,
                "detach" to 3
        )
    }

    override fun receiveCommand(root: BLVideoViewGroup, commandId: Int, args: ReadableArray?) {
        val boostlingoSdkModule  = reactContext.catalystInstance.getNativeModule("BoostlingoSdk") as BoostlingoSdkModule
        when(commandId) {
            1 -> {
                root.getSurfaceViewRenderer()?.applyZOrder(true);
                boostlingoSdkModule.setLocalVideo(root.getSurfaceViewRenderer())
            }
            2 -> {
                boostlingoSdkModule.setRemoteVideo(root.getSurfaceViewRenderer())
            }
            3 -> {
                boostlingoSdkModule.detachVideoView(root.getSurfaceViewRenderer())
            }
        }
    }
}