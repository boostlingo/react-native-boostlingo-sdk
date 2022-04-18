package com.reactnativeboostlingosdk

import android.view.ViewGroup
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.uimanager.ThemedReactContext
import com.twilio.video.VideoFrame
import com.twilio.video.VideoRenderer
import com.twilio.video.VideoScaleType
import com.twilio.video.VideoView
import tvi.webrtc.RendererCommon
import tvi.webrtc.RendererCommon.ScalingType

class BLVideoViewGroup(themedReactContext: ThemedReactContext) : ViewGroup(themedReactContext) {
    private var surfaceViewRenderer: VideoView? = null
    private var videoWidth = 0
    private var videoHeight = 0
    private val layoutSync = Any()
    private var scalingType = ScalingType.SCALE_ASPECT_FILL

    fun getSurfaceViewRenderer(): VideoView? {
        return surfaceViewRenderer
    }

    fun setScalingType(scalingType: ScalingType) {
        this.scalingType = scalingType
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var l = l
        var t = t
        var r = r
        var b = b
        val height = b - t
        val width = r - l
        if (height == 0 || width == 0) {
            b = 0
            r = b
            t = r
            l = t
        } else {
            var videoHeight: Int
            var videoWidth: Int
            synchronized(layoutSync) {
                videoHeight = this.videoHeight
                videoWidth = this.videoWidth
            }
            if (videoHeight == 0 || videoWidth == 0) {
                // These are Twilio defaults.
                videoHeight = 480
                videoWidth = 640
            }
            val displaySize = RendererCommon.getDisplaySize(
                    scalingType,
                    videoWidth / videoHeight.toFloat(),
                    width,
                    height
            )
            l = (width - displaySize.x) / 2
            t = (height - displaySize.y) / 2
            r = l + displaySize.x
            b = t + displaySize.y
        }
        surfaceViewRenderer?.layout(l, t, r, b)
    }

    init {
        surfaceViewRenderer = VideoView(themedReactContext)
        surfaceViewRenderer?.setVideoScaleType(VideoScaleType.ASPECT_FILL)
        addView(surfaceViewRenderer)
        surfaceViewRenderer?.setListener(
                object : VideoRenderer.Listener {
                    override fun onFirstFrame() {}
                    override fun onFrameDimensionsChanged(vw: Int, vh: Int, rotation: Int) {
                        synchronized(layoutSync) {
                            if (rotation == VideoFrame.RotationAngle.ROTATION_90.value ||
                                    rotation == VideoFrame.RotationAngle.ROTATION_270.value) {
                                videoHeight = vw
                                videoWidth = vh
                            } else {
                                videoHeight = vh
                                videoWidth = vw
                            }
                            forceLayout()
                            val event: WritableMap = WritableNativeMap()
                            event.putInt("height", vh)
                            event.putInt("width", vw)
                            event.putInt("rotation", rotation)
//                            pushEvent(this@BLVideoViewGroup, ON_FRAME_DIMENSIONS_CHANGED, event)
                        }
                    }
                }
        )
    }
}