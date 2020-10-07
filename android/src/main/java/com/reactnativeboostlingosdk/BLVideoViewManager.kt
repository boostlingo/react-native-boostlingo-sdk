package com.reactnativeboostlingosdk

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext

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

    override fun receiveCommand(root: RNVideoViewGroup, commandId: String?, args: ReadableArray?) {
        val boostlingoSdkModule = reactContext.getNativeModule(BoostlingoSdkModule::class.java) as BoostlingoSdkModule
        when(commandId) {
            "1" -> boostlingoSdkModule.setLocalVideo(root.getSurfaceViewRenderer())
            "2" -> boostlingoSdkModule.setRemoteVideo(root.getSurfaceViewRenderer())
            "3" -> boostlingoSdkModule.detachVideoView(root.getSurfaceViewRenderer())
        }
    }
}