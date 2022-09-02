package com.awesometsproject

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext

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
      "attachAsLocalRenderer" to 1,
      "attachAsRemoteRenderer" to 2
    )
  }

  override fun receiveCommand(root: BLVideoViewGroup, commandId: String?, args: ReadableArray?) {
    val boostlingoSdkModule  = reactContext.catalystInstance.getNativeModule("BoostlingoSdk") as BoostlingoSdkModule
    when(commandId) {
      "attachAsLocalRenderer" -> {
        root.getSurfaceViewRenderer()?.applyZOrder(true);
        boostlingoSdkModule.setLocalVideo(root.getSurfaceViewRenderer())
      }
      "attachAsRemoteRenderer" -> {
        args?.getString(0)?.let {
          boostlingoSdkModule.setRemoteVideo(it, root.getSurfaceViewRenderer())
        }
      }
    }
  }

  override fun receiveCommand(root: BLVideoViewGroup, commandId: Int, args: ReadableArray?) {
    val boostlingoSdkModule  = reactContext.catalystInstance.getNativeModule("BoostlingoSdk") as BoostlingoSdkModule
    when(commandId) {
      1 -> {
        root.getSurfaceViewRenderer()?.applyZOrder(true);
        boostlingoSdkModule.setLocalVideo(root.getSurfaceViewRenderer())
      }
      2 -> {
        args?.getString(0)?.let {
          boostlingoSdkModule.setRemoteVideo(it, root.getSurfaceViewRenderer())
        }
      }
    }
  }
}
