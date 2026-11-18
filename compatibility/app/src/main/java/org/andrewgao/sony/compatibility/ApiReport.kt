package org.andrewgao.sony.compatibility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Environment
import android.view.KeyEvent
import android.widget.ImageView
import com.github.ma1co.openmemories.framework.AudioDevices
import com.github.ma1co.openmemories.framework.Camera
import com.github.ma1co.openmemories.framework.CameraKeys
import com.github.ma1co.openmemories.framework.CameraNotices
import com.github.ma1co.openmemories.framework.CameraParameters
import com.github.ma1co.openmemories.framework.CameraSystem
import com.github.ma1co.openmemories.framework.CaptureSequence
import com.github.ma1co.openmemories.framework.DateTime
import com.github.ma1co.openmemories.framework.DeviceInfo
import com.github.ma1co.openmemories.framework.DisplayManager
import com.github.ma1co.openmemories.framework.FaceFinder
import com.github.ma1co.openmemories.framework.HighlightMovie
import com.github.ma1co.openmemories.framework.ImageDisplay
import com.github.ma1co.openmemories.framework.ImageFilters
import com.github.ma1co.openmemories.framework.IndicatorLights
import com.github.ma1co.openmemories.framework.JpegSaver
import com.github.ma1co.openmemories.framework.MediaManager
import com.github.ma1co.openmemories.framework.MoviePlayer
import com.github.ma1co.openmemories.framework.MovieRecorder
import com.github.ma1co.openmemories.framework.SignalProcessor
import com.github.ma1co.openmemories.framework.StillImage
import java.io.ByteArrayInputStream
import java.io.File

/**
 * Calls each framework API inside its own try/catch.
 * On a Sony camera, a call that returns a result is compatible. A getSupported* or is*Supported
 * query is compatible only when that result is a real value.
 * A missing class or method is incompatible. Any other failure, including a timeout, is skipped.
 * Shutter, card format, factory reset, and power-off are not fired.
 */
class ApiReport(
    private val context: Context,
    private val preview: () -> android.view.SurfaceHolder? = { null },
    private val onProgress: (name: String, index: Int, total: Int) -> Unit = { _, _, _ -> }
) {
    private enum class Kind { CALL, SUPPORTED }

    private class Line(val status: String, val name: String, val detail: String)

    private val lines = ArrayList<Line>()
    private var sony = false
    private var done = 0
    private val total = 289

    fun run(): ApiReport {
        try {
            sony = DeviceInfo.getInstance().isCamera()
        } catch (ignored: Throwable) {
            sony = false
        }
        lines.add(Line("INFO", "device", deviceLine()))
        probeDevice()
        probeDisplay()
        probeMedia()
        probeSystem()
        probeKeysAndLights()
        val surfaceDeadline = System.currentTimeMillis() + 2000
        while (preview() == null && System.currentTimeMillis() < surfaceDeadline) Thread.sleep(50)
        val camera = probeCamera()
        probeParameters(camera)
        probeSequence(camera)
        probe("Camera.release") {
            val opened = camera ?: throw IllegalStateException("camera not open")
            opened.release()
        }
        probeImages()
        probeSignal()
        probeAudio()
        probeMovies()
        return this
    }

    fun lines(): List<String> {
        val out = ArrayList<String>()
        out.add(summary())
        for (line in lines) out.add(format(line))
        return out
    }

    fun text(): String = lines().joinToString("\n")

    fun summary(): String {
        var compatible = 0
        var incompatible = 0
        var skipped = 0
        for (line in lines) {
            when (line.status) {
                "COMPATIBLE" -> compatible++
                "INCOMPATIBLE" -> incompatible++
                "SKIPPED" -> skipped++
            }
        }
        return "COMPATIBLE $compatible\nINCOMPATIBLE $incompatible\nSKIPPED $skipped\ncamera=$sony"
    }

    fun writeToSdcard(): String {
        val roots = ArrayList<File>()
        try {
            val external = Environment.getExternalStorageDirectory()
            if (external != null) roots.add(external)
        } catch (ignored: Throwable) {
        }
        roots.add(File("/mnt/sdcard"))
        // FuFsys rejects long names. TWEAKLOG.TXT on this card is an 8.3 name.
        val relative = arrayOf(
            "COMPAT.TXT",
            "PRIVATE/COMPAT.TXT",
            "MP_ROOT/COMPAT.TXT"
        )
        val errors = ArrayList<String>()
        val seen = HashSet<String>()
        for (root in roots) {
            for (name in relative) {
                val file = File(root, name)
                if (!seen.add(file.absolutePath)) continue
                val parent = file.parentFile
                if (parent == null || !parent.isDirectory) {
                    errors.add(file.absolutePath + " parent missing")
                    continue
                }
                try {
                    file.writeText(text())
                    return "wrote " + file.absolutePath
                } catch (t: Throwable) {
                    errors.add(file.absolutePath + " " + describe(t))
                }
            }
        }
        return "sdcard log not written: " + errors.joinToString("; ")
    }

    private fun deviceLine(): String {
        return "brand=" + Build.BRAND + " model=" + Build.MODEL + " device=" + Build.DEVICE + " sdk=" + Build.VERSION.SDK_INT
    }

    private fun probeDevice() {
        val info = try {
            DeviceInfo.getInstance()
        } catch (t: Throwable) {
            fail("DeviceInfo.getInstance", t)
            null
        }
        fun device() = need(info, "device info not open")
        probe("DeviceInfo.isCamera", Kind.SUPPORTED) { device().isCamera }
        probe("DeviceInfo.getBrand") { device().brand }
        probe("DeviceInfo.getModel") { device().model }
        probe("DeviceInfo.getCategory") { device().category }
        probe("DeviceInfo.getProductCode") { device().productCode }
        probe("DeviceInfo.getSerialNumber") { device().serialNumber }
        probe("DeviceInfo.getFirmwareVersion") { device().firmwareVersion }
        probe("DeviceInfo.getHardwareVersion") { device().hardwareVersion }
        probe("DeviceInfo.getApiVersion") { device().apiVersion }
        probe("DeviceInfo.getAndroidVersion") { device().androidVersion }
        probe("DeviceInfo.getAndroidSdkVersion") { device().androidSdkVersion }
        probe("DeviceInfo.getPlatformVersion") { device().platformVersion }
        probe("DeviceInfo.isWifiSupported", Kind.SUPPORTED) { device().isWifiSupported }
        probe("DeviceInfo.hasMechanicalShutter", Kind.SUPPORTED) { device().hasMechanicalShutter() }
        probe("DeviceInfo.getSensorMegapixels") { device().sensorMegapixels }
        probe("DeviceInfo.getSupportedPictureSizes", Kind.SUPPORTED) { device().supportedPictureSizes }
        probe("DeviceInfo.getProperty", Kind.SUPPORTED) { device().getProperty(DeviceInfo.PROP_MODEL_NAME) }
        probe("DeviceInfo.getIntProperty") { device().getIntProperty(DeviceInfo.PROP_SENSOR_MEGAPIXELS, -1) }
        probe("DeviceInfo.getWifiProductCode") { device().wifiProductCode }
        probe("DateTime.getCurrentTime") { DateTime.getInstance().currentTime.timeInMillis }
        probe("DateTime.getGmtOffsetMinutes") { DateTime.getInstance().gmtOffsetMinutes }
    }

    private fun probeDisplay() {
        val opened = openDevice("DisplayManager.create", "created") { DisplayManager.create(context) }
        val display = opened.value
        fun view() = need(display, "display not created", opened.error)
        try {
            probe("DisplayManager.getActiveDisplay") { view().activeDisplay }
            probe("DisplayManager.getActiveDisplayInfo") {
                val info = view().activeDisplayInfo
                info.width.toString() + "x" + info.height
            }
            probe("DisplayManager.getDisplayInfo") { view().getDisplayInfo(DisplayManager.Display.SCREEN).width }
            probe("DisplayManager.getFrameBufferInfo") { view().frameBufferInfo.width }
            probe("DisplayManager.setActiveDisplay") { view().setActiveDisplay(DisplayManager.Display.SCREEN) }
            probe("DisplayManager.setColorDepth") { view().setColorDepth(DisplayManager.ColorDepth.LOW) }
            probe("DisplayManager.addListener") {
                val listener = object : DisplayManager.Listener {
                    override fun displayChanged(changed: DisplayManager.Display) {}
                }
                view().addListener(listener)
                view().removeListener(listener)
            }
            probe("DisplayManager.addEventListener") {
                val listener = object : DisplayManager.EventListener {
                    override fun onDisplayEvent(event: Int) {}
                }
                view().addEventListener(listener)
                view().removeEventListener(listener)
            }
            probe("DisplayManager.startQfhdOutput") { view().startQfhdOutput() }
            probe("DisplayManager.stopQfhdOutput") { view().stopQfhdOutput() }
        } finally {
            try {
                display?.release()
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun probeMedia() {
        val opened = openDevice("MediaManager.create", "created") { MediaManager.create(context) }
        val media = opened.value
        fun card() = need(media, "media not open", opened.error)
        probe("MediaManager.getMediaIds", Kind.SUPPORTED) { card().mediaIds.toList() }
        probe("MediaManager.getVirtualMediaIds") { card().virtualMediaIds.toList() }
        probe("MediaManager.getAvailableBytes") {
            val ids = try { card().mediaIds } catch (ignored: Throwable) { arrayOf("external") }
            val id = if (ids.isNotEmpty()) ids[0] else "external"
            card().getAvailableBytes(id)
        }
        probe("MediaManager.getMediaType") { card().getMediaType("external") }
        probe("MediaManager.isEyeFi") { card().isEyeFi("external") }
        probe("MediaManager.queryNewestImages") {
            val cursor = card().queryNewestImages()
            val count = cursor?.count ?: 0
            cursor?.close()
            count
        }
        probe("MediaManager.queryNewestVideos") {
            val cursor = card().queryNewestVideos()
            val count = cursor?.count ?: 0
            cursor?.close()
            count
        }
        probe("MediaManager.loadMedia") { card().loadMedia("external", MediaManager.CONTENT_STILL) }
        probe("MediaManager.waitForDatabase") { card().waitForDatabase("external") }
        probe("MediaManager.rotateImage") { card().rotateImage(-1, 90) }
    }

    private fun probeSystem() {
        probe("CameraSystem.notifyRunning") { CameraSystem.notifyRunning(context) }
        probe("CameraSystem.setAutoPowerOffEnabled") { CameraSystem.setAutoPowerOffEnabled(context, true) }
        val opened = openDevice("CameraSystem.open", "open") { CameraSystem.open(context) }
        val system = opened.value
        fun sys() = need(system, "camera system not open", opened.error)
        try {
            probe("CameraSystem.getAutoPowerOffSeconds") { sys().autoPowerOffSeconds }
            probe("CameraSystem.setAutoPowerOffSeconds") { sys().autoPowerOffSeconds = 300 }
            probe("CameraSystem.getSupportedAutoPowerOffTimes", Kind.SUPPORTED) { sys().supportedAutoPowerOffTimes }
            probe("CameraSystem.getAutoRotate") { sys().autoRotate }
            probe("CameraSystem.getDialSetting") { sys().dialSetting }
            probe("CameraSystem.getGridLine") { sys().gridLine }
            probe("CameraSystem.getTargetMediaId") { sys().targetMediaId }
            probe("CameraSystem.setKeyLock") { sys().setKeyLock(false) }
            probe("CameraSystem.isKeyLocked") { sys().isKeyLocked }
            probe("CameraSystem.isSystemReady", Kind.SUPPORTED) { sys().isSystemReady }
            probe("CameraSystem.getBatteryPercent") { sys().batteryPercent }
            probe("CameraSystem.setBatteryListener") {
                sys().setBatteryListener(object : CameraSystem.BatteryListener {
                    override fun onRemainTime(seconds: Int) {}
                })
            }
            probe("CameraSystem.getTemperatureStatus") { sys().temperatureStatus }
            probe("CameraSystem.setTemperatureListener") {
                sys().setTemperatureListener(object : CameraSystem.TemperatureListener {
                    override fun onStatus(status: Int) {}
                    override fun onShutdownCountdown(enabled: Boolean, seconds: Long) {}
                })
            }
            probe("CameraSystem.getGpsPower") { sys().gpsPower }
            probe("CameraSystem.getGpsState") { sys().gpsState }
            probe("CameraSystem.startGps") { sys().startGps() }
            probe("CameraSystem.stopGps") { sys().stopGps() }
            probe("CameraSystem.isNfcEnabled", Kind.SUPPORTED) { sys().isNfcEnabled }
            probe("CameraSystem.startNfc") { sys().startNfc() }
            probe("CameraSystem.stopNfc") { sys().stopNfc() }
            probe("CameraSystem.isBluetoothOn", Kind.SUPPORTED) { sys().isBluetoothOn }
            probe("CameraSystem.isBluetoothConnected", Kind.SUPPORTED) { sys().isBluetoothConnected }
            probe("CameraSystem.log") { sys().log(0) }
            destructive("CameraSystem.formatCard") { sys().formatCard() }
            destructive("CameraSystem.recoverCard") { sys().recoverCard(CameraSystem.CARD_RECOVERY) }
            destructive("CameraSystem.resetAll") { sys().resetAll() }
            destructive("CameraSystem.requestPowerOff") { sys().requestPowerOff() }
        } finally {
            try {
                system?.release()
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun probeKeysAndLights() {
        probe("CameraKeys.fromKeyEvent") {
            CameraKeys.fromKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))
        }
        probe("CameraKeys.getStatus") { CameraKeys.getStatus(CameraKeys.SHUTTER) }
        probe("IndicatorLights.initialize") { IndicatorLights.initialize() }
        probe("IndicatorLights.set") { IndicatorLights.set(IndicatorLights.LED_SELF_TIMER, false) }
        probe("IndicatorLights.setPattern") {
            IndicatorLights.set(IndicatorLights.LED_SELF_TIMER, false, IndicatorLights.PATTERN_OFF)
        }
        probe("IndicatorLights.setSubLcd") { IndicatorLights.setSubLcd("") }
        probe("IndicatorLights.hdmiOneTouchPlay") { IndicatorLights.hdmiOneTouchPlay() }
        probe("IndicatorLights.release") { IndicatorLights.release() }
        probe("CameraNotices.setEnabled") { CameraNotices.setEnabled(0, false) }
        probe("CameraNotices.setFactor") { CameraNotices.setFactor(0, false) }
        probe("CameraNotices.setTrigger") { CameraNotices.setTrigger(0, 0, false) }
    }

    private fun probeCamera(): Camera? {
        var camera: Camera? = null
        var cameraError: Throwable? = null
        probe("Camera.open") {
            try {
                camera = retryOpen { onMain { Camera.open(null) } }
                "open"
            } catch (t: Throwable) {
                cameraError = t
                throw t
            }
        }
        fun cam(): Camera {
            val opened = camera
            if (opened != null) return opened
            throw cameraError ?: IllegalStateException("camera not open")
        }
        probe("Camera.setPreviewDisplay") {
            val holder = preview() ?: throw IllegalStateException("preview surface not ready")
            cam().setPreviewDisplay(holder)
        }
        probe("Camera.startPreview") { cam().startPreview() }
        probe("Camera.getNormalCamera") { cam().normalCamera != null }
        probe("Camera.getLens") { cam().lens.name }
        probe("Camera.getExternalFlash") { cam().externalFlash.afLightAuto }
        probe("Camera.getPreviewColor") {
            val color = cam().getPreviewColor(0, 0)
            color.y
        }
        probe("Camera.setColorSelect") { cam().setColorSelect(0, Camera.SelectedColor()) }
        probe("Camera.createGammaCurve") {
            val curve = cam().createGammaCurve()
            curve.write(ByteArrayInputStream(ByteArray(8)))
            cam().setExtendedGamma(curve)
            cam().setExtendedGamma(null)
            "gamma"
        }
        probe("Camera.setPreviewMagnification") { cam().setPreviewMagnification(1, 0, 0) }
        probe("Camera.stopPreviewMagnification") { cam().stopPreviewMagnification() }
        probe("Camera.startZoom") { cam().startZoom(1, 1) }
        probe("Camera.stopZoom") { cam().stopZoom() }
        probe("Camera.incrementShutterSpeed") { cam().incrementShutterSpeed() }
        probe("Camera.decrementShutterSpeed") { cam().decrementShutterSpeed() }
        probe("Camera.adjustShutterSpeed") { cam().adjustShutterSpeed(0) }
        probe("Camera.incrementAperture") { cam().incrementAperture() }
        probe("Camera.decrementAperture") { cam().decrementAperture() }
        probe("Camera.adjustAperture") { cam().adjustAperture(0) }
        probe("Camera.shiftFocus") { cam().shiftFocus(0) }
        probe("Camera.startOneShotFocusDrive") { cam().startOneShotFocusDrive(Camera.FOCUS_DIRECTION_NEAR, 1) }
        probe("Camera.startFocusHold") { cam().startFocusHold() }
        probe("Camera.stopFocusHold") { cam().stopFocusHold() }
        probe("Camera.startFaceDetection") { cam().startFaceDetection() }
        probe("Camera.stopFaceDetection") { cam().stopFaceDetection() }
        probe("Camera.cancelAutoFocus") { cam().cancelAutoFocus() }
        probe("Camera.cancelTakePicture") { cam().cancelTakePicture() }
        probe("Camera.cancelExposure") { cam().cancelExposure() }
        probe("Camera.stopDirectShutter") {
            cam().stopDirectShutter(object : Camera.DirectShutterCallback {
                override fun onStopped() {}
            })
        }
        probe("Camera.cancelSelfTimerShutter") { cam().cancelSelfTimerShutter() }
        destructive("Camera.takePicture") { cam().takePicture() }
        destructive("Camera.burstableTakePicture") { cam().burstableTakePicture() }
        destructive("Camera.startDirectShutter") { cam().startDirectShutter() }
        destructive("Camera.autoFocus") { cam().autoFocus() }
        destructive("Camera.startSelfTimerShutter") { cam().startSelfTimerShutter() }
        probe("Camera.stopPreview") { cam().stopPreview() }
        probe("Camera.setShutterListener") {
            cam().setShutterListener(object : Camera.ShutterListener {
                override fun onShutter(status: Int) {}
            })
        }
        probe("Camera.setCaptureStatusListener") {
            cam().setCaptureStatusListener(object : Camera.CaptureStatusListener {
                override fun onStart(reason: Int) {}
                override fun onEnd(reason: Int, extra: Int) {}
            })
        }
        probe("Camera.setJpegListener") {
            cam().setJpegListener(object : Camera.JpegListener {
                override fun onJpeg(data: ByteArray) {}
            })
        }
        probe("Camera.setAutoFocusListener") {
            cam().setAutoFocusListener(object : Camera.AutoFocusListener {
                override fun onAutoFocus(success: Boolean) {}
            })
        }
        probe("Camera.setFocusListener") {
            cam().setFocusListener(object : Camera.FocusListener {
                override fun onFocus(position: Int, maxPosition: Int) {}
            })
        }
        probe("Camera.setApertureListener") {
            cam().setApertureListener(object : Camera.ApertureListener {
                override fun onAperture(aperture: Int) {}
            })
        }
        probe("Camera.setShutterSpeedListener") {
            cam().setShutterSpeedListener(object : Camera.ShutterSpeedListener {
                override fun onShutterSpeed(numerator: Int, denominator: Int) {}
            })
        }
        probe("Camera.setZoomListener") {
            cam().setZoomListener(object : Camera.ZoomListener {
                override fun onZoom(opticalPosition: Int, digitalPosition: Int, stopped: Boolean) {}
            })
        }
        probe("Camera.setFaceListener") {
            cam().setFaceListener(object : Camera.FaceListener {
                override fun onFaces(faces: List<Rect>) {}
            })
        }
        probe("Camera.setStoredImageListener") {
            cam().setStoredImageListener(object : Camera.StoredImageListener {
                override fun onStored(status: Int, directory: String?, filename: String?) {}
            })
        }
        return camera
    }

    private fun probeParameters(camera: Camera?) {
        val parameters = if (camera == null) null else try {
            camera.parameters
        } catch (t: Throwable) {
            fail("Camera.getParameters", t)
            null
        }
        if (parameters != null) probe("Camera.getParameters") { "parameters" }
        fun params() = need(parameters, "parameters not open")
        supported("CameraParameters.getSupportedWhiteBalance", { params().supportedWhiteBalance }) {
            params().whiteBalance = it
        }
        supported("CameraParameters.getSupportedFlashModes", { params().supportedFlashModes }) {
            params().flashMode = it
        }
        supported("CameraParameters.getSupportedFocusModes", { params().supportedFocusModes }) {
            params().focusMode = it
        }
        supported("CameraParameters.getSupportedSceneModes", { params().supportedSceneModes }) {
            params().sceneMode = it
        }
        supported("CameraParameters.getSupportedIso", { params().supportedIso.map { it.toString() } }) {
            params().iso = it.toInt()
        }
        supported("CameraParameters.getSupportedDriveModes", { params().supportedDriveModes }) {
            params().driveMode = it
        }
        supported("CameraParameters.getSupportedPictureEffects", { params().supportedPictureEffects }) {
            params().pictureEffect = it
        }
        supported("CameraParameters.getSupportedImageSizes", { params().supportedImageSizes }) {
            params().imageSize = it
        }
        supported("CameraParameters.getSupportedAspectRatios", { params().supportedAspectRatios }) {
            params().aspectRatio = it
        }
        supported("CameraParameters.getSupportedMeteringModes", { params().supportedMeteringModes }) {
            params().meteringMode = it
        }
        supported("CameraParameters.getSupportedColorDepthTypes", { params().supportedColorDepthTypes }) {
            params().setColorDepth(it, 0)
        }
        supported("CameraParameters.getSupportedLensCorrections", { params().supportedLensCorrections }) {
            params().setLensCorrectionLevel(it, 0)
        }
        probe("CameraParameters.isAutoWhiteBalanceLockSupported", Kind.SUPPORTED) { params().isAutoWhiteBalanceLockSupported }
        probe("CameraParameters.isRemoteControlModeSupported", Kind.SUPPORTED) { params().isRemoteControlModeSupported }
        probe("CameraParameters.isRgbMatrixSupported", Kind.SUPPORTED) { params().isRgbMatrixSupported }
        probe("CameraParameters.isSharpnessGainModeSupported", Kind.SUPPORTED) { params().isSharpnessGainModeSupported }
        probe("CameraParameters.isWhiteBalanceShiftModeSupported", Kind.SUPPORTED) { params().isWhiteBalanceShiftModeSupported }
        probe("CameraParameters.isExtendedGammaSupported", Kind.SUPPORTED) { params().isExtendedGammaSupported }
        probe("CameraParameters.isExposureShiftSupported", Kind.SUPPORTED) { params().isExposureShiftSupported }
        probe("CameraParameters.getAperture") { params().aperture }
        probe("CameraParameters.getShutterSpeed") { params().shutterNumerator.toString() + "/" + params().shutterDenominator }
        probe("CameraParameters.getExposureCompensation") { params().exposureCompensation }
        probe("CameraParameters.setContrast") { params().contrast = 0 }
        probe("CameraParameters.setSaturation") { params().saturation = 0 }
        probe("CameraParameters.setSharpness") { params().sharpness = 0 }
        probe("CameraParameters.setAutoExposureLock") { params().autoExposureLock = CameraParameters.AE_LOCK_OFF }
        probe("CameraParameters.setAutoWhiteBalanceLock") { params().autoWhiteBalanceLock = false }
        probe("CameraParameters.setSilentShutter") { params().isSilentShutter = false }
        probe("CameraParameters.setColorTemperature") { params().colorTemperature = 5500 }
        probe("CameraParameters.apply") { params().apply() }
        probe("CameraParameters.getIso") { params().iso }
        probe("CameraParameters.getDriveMode") { params().driveMode }
        probe("CameraParameters.getPictureEffect") { params().pictureEffect }
        probe("CameraParameters.getImageSize") { params().imageSize }
        probe("CameraParameters.getAspectRatio") { params().aspectRatio }
        probe("CameraParameters.getMeteringMode") { params().meteringMode }
        probe("CameraParameters.getDroMode") { params().droMode }
        probe("CameraParameters.setDroMode") { params().droMode = "off" }
        probe("CameraParameters.getContrast") { params().contrast }
        probe("CameraParameters.getSaturation") { params().saturation }
        probe("CameraParameters.getSharpness") { params().sharpness }
        probe("CameraParameters.getFaceDetectionMode") { params().faceDetectionMode }
        probe("CameraParameters.setFaceDetectionMode") { params().faceDetectionMode = "off" }
        probe("CameraParameters.getAutoFocusMode") { params().autoFocusMode }
        probe("CameraParameters.setAutoFocusMode") { params().autoFocusMode = "af-s" }
        probe("CameraParameters.getAntiHandBlurMode") { params().antiHandBlurMode }
        probe("CameraParameters.setAntiHandBlurMode") { params().antiHandBlurMode = "off" }
        probe("CameraParameters.getRemoteControlMode") { params().remoteControlMode }
        probe("CameraParameters.setRemoteControlMode") { params().remoteControlMode = false }
        probe("CameraParameters.getRgbMatrix") { params().rgbMatrix?.size ?: 0 }
        probe("CameraParameters.getLongExposureNoiseReduction") { params().longExposureNoiseReduction }
        probe("CameraParameters.getColorSelectMode") { params().colorSelectMode }
        probe("CameraParameters.getSharpnessGainMode") { params().sharpnessGainMode }
        probe("CameraParameters.setSharpnessGainMode") { params().sharpnessGainMode = false }
        probe("CameraParameters.getSharpnessGain") { params().sharpnessGain }
        probe("CameraParameters.setSharpnessGain") { params().sharpnessGain = 0 }
        probe("CameraParameters.getWhiteBalanceShiftMode") { params().whiteBalanceShiftMode }
        probe("CameraParameters.setWhiteBalanceShiftMode") { params().whiteBalanceShiftMode = false }
        probe("CameraParameters.getWhiteBalanceShiftAmberBlue") { params().whiteBalanceShiftAmberBlue }
        probe("CameraParameters.setWhiteBalanceShiftAmberBlue") { params().whiteBalanceShiftAmberBlue = 0 }
        probe("CameraParameters.getWhiteBalanceShiftGreenMagenta") { params().whiteBalanceShiftGreenMagenta }
        probe("CameraParameters.setWhiteBalanceShiftGreenMagenta") { params().whiteBalanceShiftGreenMagenta = 0 }
        probe("CameraParameters.getExposureShift") { params().exposureShift }
        probe("CameraParameters.setExposureShift") { params().exposureShift = 0 }
        probe("CameraParameters.getFocusAreaMode") { params().focusAreaMode }
        probe("CameraParameters.getFocusPointIndex") { params().focusPointIndex }
        probe("CameraParameters.getLensCorrection") { params().lensCorrection }
        probe("CameraParameters.setLensCorrection") { params().setLensCorrection(false) }
        probe("CameraParameters.getColorTemperature") { params().colorTemperature }
        probe("CameraParameters.getJpegQuality") { params().jpegQuality }
        probe("CameraParameters.setJpegQuality") { params().jpegQuality = 90 }
        probe("CameraParameters.getPictureSize") { params().pictureWidth.toString() + "x" + params().pictureHeight }
        probe("CameraParameters.getMinExposureCompensation") { params().minExposureCompensation }
        probe("CameraParameters.getMaxExposureCompensation") { params().maxExposureCompensation }
    }

    private fun probeSequence(camera: Camera?) {
        val sequenceOpen = if (camera == null) {
            probe("CaptureSequence.open") { throw IllegalStateException("camera not open") }
            Opened<CaptureSequence>(null, IllegalStateException("camera not open"))
        } else {
            openDevice("CaptureSequence.open", "open") { CaptureSequence.open(camera) }
        }
        val sequence = sequenceOpen.value
        fun seq() = need(sequence, "capture sequence not open", sequenceOpen.error)
        val processorOpen = openDevice("SignalProcessor.create", "created") { SignalProcessor.create() }
        val processor = processorOpen.value
        try {
            probe("CaptureSequence.setOptions") { seq().setOptions(1, 1, null) }
            probe("CaptureSequence.setShutterListener") {
                seq().setShutterListener(object : CaptureSequence.ShutterListener {
                    override fun onFrame(frame: CaptureSequence.RawFrame) {}
                })
            }
            probe("CaptureSequence.setPreviewProcessor") { seq().setPreviewProcessor(processor) }
            probe("CaptureSequence.startPreview") { seq().startPreview(30, 640, 480) }
            probe("CaptureSequence.stopPreview") { seq().stopPreview() }
            probe("CaptureSequence.develop") { seq().develop(null) }
        } finally {
            try {
                sequence?.release()
            } catch (ignored: Throwable) {
            }
            try {
                processor?.release()
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun probeImages() {
        var still: StillImage? = null
        probe("StillImage.fromBitmap") {
            still = StillImage.fromBitmap(Bitmap.createBitmap(16, 16, Bitmap.Config.RGB_565))
            still?.width
        }
        val image = still
        fun stillImage() = need(image, "still image not open")
            probe("StillImage.getWidth") { stillImage().width }
            probe("StillImage.getHeight") { stillImage().height }
            probe("StillImage.isValid", Kind.SUPPORTED) { stillImage().isValid }
            probe("StillImage.toJpeg") { stillImage().toJpeg(90)?.size ?: 0 }
            probe("ImageFilters.scale") { ImageFilters.scale(stillImage(), 8, 8)?.width }
            probe("ImageFilters.crop") { ImageFilters.crop(stillImage(), Rect(0, 0, 8, 8))?.width }
            probe("ImageFilters.rotate") { ImageFilters.rotate(stillImage(), 90.0)?.width }
            probe("ImageFilters.miniature") { ImageFilters.miniature(stillImage(), ImageFilters.MINIATURE_HORIZONTAL_CENTER)?.width }
            probe("ImageFilters.faceNoiseReduction") {
                ImageFilters.faceNoiseReduction(stillImage(), listOf(Rect(0, 0, 8, 8)), 100, ImageFilters.NR_LOW)?.width
            }
            probe("ImageFilters.contrast") { ImageFilters.contrast(stillImage(), 1)?.width }
            probe("ImageFilters.redEye") { ImageFilters.redEye(stillImage())?.width }
            probe("ImageFilters.softFocus") { ImageFilters.softFocus(stillImage(), 1)?.width }
            probe("ImageFilters.superResolution") { ImageFilters.superResolution(stillImage())?.width }
            probe("FaceFinder.find") { FaceFinder.find(stillImage())?.size ?: 0 }
            probe("JpegSaver.save") { JpegSaver.save(context, stillImage(), "external", JpegSaver.QUALITY_STANDARD) }
            probe("ImageDisplay.attach") {
                val display = ImageDisplay.attach(ImageView(context))
                display.setImage(stillImage())
                display.setFit()
                display.setScale(1f)
                display.pan(0, 0)
                display.redraw()
                val pan = display.canPanRight()
                display.release()
                pan
            }
            try {
                image?.release()
            } catch (ignored: Throwable) {
            }
        probe("StillImage.decodeFile") { StillImage.decodeFile("/mnt/sdcard/DCIM/100MSDCF/DSC00001.JPG", StillImage.TYPE_SCREEN)?.width }
        probe("StillImage.fromJpeg") { StillImage.fromJpeg(ByteArray(0))?.isValid }
    }

    private fun probeSignal() {
        probe("SignalProcessor.toAxiAddress") { SignalProcessor.toAxiAddress(0, false) }
        probe("SignalProcessor.setAllocationPolicy") { SignalProcessor.setAllocationPolicy(SignalProcessor.ALLOCATION_VERSION_1) }
        probe("SignalProcessor.loadMemoryMap") { SignalProcessor.loadMemoryMap(null) }
        val processorOpen = openDevice("SignalProcessor.create", "created") { SignalProcessor.create() }
        val processor = processorOpen.value
        fun dsp() = need(processor, "signal processor not open", processorOpen.error)
        try {
            probe("SignalProcessor.isHardware", Kind.SUPPORTED) { dsp().isHardware }
            probe("SignalProcessor.setProgram") { dsp().program = "probe" }
            probe("SignalProcessor.getProgram") { dsp().program }
            probe("SignalProcessor.getIntProperty") { dsp().getIntProperty(SignalProcessor.PROPERTY_PROGRAM) }
            probe("SignalProcessor.createBuffer") {
                val buffer = dsp().createBuffer(16)
                buffer?.size
            }
            probe("SignalProcessor.createImage") {
                val created = dsp().createImage(16, 16)
                val width = created?.width
                created?.release()
                width
            }
            probe("SignalProcessor.setArgument") { dsp().setArgument(0, 1) }
            probe("SignalProcessor.getArgument") { dsp().getArgument(0) }
            probe("SignalProcessor.execute", Kind.SUPPORTED) { dsp().execute() }
            probe("SignalProcessor.clearProgram") { dsp().clearProgram() }
        } finally {
            try {
                processor?.release()
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun probeAudio() {
        val opened = openDevice("AudioDevices.create", "created") { AudioDevices.create() }
        val audio = opened.value
        fun mic() = need(audio, "audio not open", opened.error)
        try {
            probe("AudioDevices.setMicrophoneEnabled") { mic().isMicrophoneEnabled = false }
            probe("AudioDevices.isMicrophoneEnabled") { mic().isMicrophoneEnabled }
            probe("AudioDevices.setSoundVolume") { mic().soundVolume = 1 }
            probe("AudioDevices.getSoundVolume") { mic().soundVolume }
            probe("AudioDevices.setOutputBeep") { mic().outputBeep = AudioDevices.BEEP_OFF }
            probe("AudioDevices.getOutputBeep") { mic().outputBeep }
            probe("AudioDevices.setWindNoiseReduction") { mic().isWindNoiseReduction = false }
            probe("AudioDevices.isWindNoiseReduction") { mic().isWindNoiseReduction }
            probe("AudioDevices.read") { mic().read(ByteArray(16)) }
            destructive("AudioDevices.startRecording") { mic().startRecording() }
            probe("AudioDevices.stopRecording") { mic().stopRecording() }
            destructive("AudioDevices.play") { mic().play() }
            probe("AudioDevices.write") { mic().write(ByteArray(16)) }
            probe("AudioDevices.stopPlayback") { mic().stopPlayback() }
        } finally {
            try {
                audio?.release()
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun probeMovies() {
        val recorderOpen = openDevice("MovieRecorder.create", "created") { MovieRecorder.create() }
        val recorder = recorderOpen.value
        fun rec() = need(recorder, "movie recorder not open", recorderOpen.error)
        try {
            probe("MovieRecorder.setOutputMedia") { rec().setOutputMedia("external") }
            probe("MovieRecorder.setOutputFile") {
                rec().setOutputFile(File(context.cacheDir, "probe.mp4").absolutePath)
            }
            probe("MovieRecorder.prepare") { rec().prepare() }
            destructive("MovieRecorder.start") { rec().start() }
            probe("MovieRecorder.stop") { rec().stop() }
        } finally {
            try {
                recorder?.release()
            } catch (ignored: Throwable) {
            }
        }
        val playerOpen = openDevice("MoviePlayer.create", "created") { MoviePlayer.create() }
        val player = playerOpen.value
        fun play() = need(player, "movie player not open", playerOpen.error)
            val movie = findMovie()
            try {
                probe("MoviePlayer.setDataSource") {
                    play().setDataSource(movie ?: File(context.cacheDir, "missing.mp4").absolutePath)
                }
                probe("MoviePlayer.prepare") { play().prepare() }
                probe("MoviePlayer.setPlaybackSpeed") { play().setPlaybackSpeed(MoviePlayer.SPEED_NORMAL) }
                probe("MoviePlayer.setDisplayMode") { play().setDisplayMode(MoviePlayer.DISPLAY_FULL) }
                probe("MoviePlayer.getDuration") { play().duration }
                probe("MoviePlayer.getCurrentPosition") { play().currentPosition }
                probe("MoviePlayer.isPlaying") { play().isPlaying }
                probe("MoviePlayer.seekTo") { play().seekTo(0) }
                probe("MoviePlayer.stepForward") { play().stepForward() }
                probe("MoviePlayer.stepRewind") { play().stepRewind() }
                probe("MoviePlayer.start") { play().start() }
                probe("MoviePlayer.pause") { play().pause() }
                probe("MoviePlayer.stop") { play().stop() }
        } finally {
            try {
                player?.release()
            } catch (ignored: Throwable) {
            }
        }
        val highlightOpen = openDevice("HighlightMovie.create", "created") { HighlightMovie.create() }
        val highlight = highlightOpen.value
        fun reel() = need(highlight, "highlight movie not open", highlightOpen.error)
        try {
            probe("HighlightMovie.isSupported", Kind.SUPPORTED) { reel().isSupported }
            probe("HighlightMovie.setBackgroundMusic") { reel().setBackgroundMusic(HighlightMovie.MUSIC_1) }
            probe("HighlightMovie.setLength") { reel().setLength(HighlightMovie.LENGTH_15_SEC) }
            probe("HighlightMovie.prepare") { reel().prepare("external", emptyList<String>()) }
            destructive("HighlightMovie.execute") { reel().execute() }
        } finally {
            try {
                highlight?.release()
            } catch (ignored: Throwable) {
            }
        }
    }

    private class Opened<T>(val value: T?, val error: Throwable?)

    private fun <T> need(value: T?, reason: String, error: Throwable? = null): T {
        if (value != null) return value
        throw error ?: IllegalStateException(reason)
    }

    /** Opens a device on the current thread, retrying while the service is still coming up. */
    private fun <T> openDevice(name: String, success: String, factory: () -> T): Opened<T> {
        var value: T? = null
        var error: Throwable? = null
        probe(name) {
            try {
                value = retryOpen { onMain { factory() } }
                success
            } catch (t: Throwable) {
                error = t
                throw t
            }
        }
        return Opened(value, error)
    }

    private fun <T> retryOpen(factory: () -> T): T {
        var last: Throwable? = null
        for (attempt in 0 until 4) {
            try {
                return factory()
            } catch (t: Throwable) {
                last = t
                if (t is LinkageError || attempt == 3) break
                try {
                    Thread.sleep(500)
                } catch (ignored: InterruptedException) {
                }
            }
        }
        throw last ?: IllegalStateException("not open")
    }

    /**
     * Opens run on the main thread. The calls after that stay off it, so a camera
     * call cannot stall the main looper waiting for its own callback.
     */
    private fun <T> onMain(block: () -> T): T {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) return block()
        val value = arrayOfNulls<Any>(1)
        var filled = false
        val error = arrayOfNulls<Throwable>(1)
        val latch = java.util.concurrent.CountDownLatch(1)
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                value[0] = block()
                filled = true
            } catch (t: Throwable) {
                error[0] = t
            } finally {
                latch.countDown()
            }
        }
        latch.await()
        val thrown = error[0]
        if (thrown != null) throw thrown
        if (!filled) throw IllegalStateException("open returned nothing")
        @Suppress("UNCHECKED_CAST")
        return value[0] as T
    }

    private fun <T> callWithTimeout(block: () -> T): T {
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) return block()
        val value = arrayOfNulls<Any>(1)
        var filled = false
        val error = arrayOfNulls<Throwable>(1)
        val latch = java.util.concurrent.CountDownLatch(1)
        Thread {
            try {
                value[0] = block()
                filled = true
            } catch (t: Throwable) {
                error[0] = t
            } finally {
                latch.countDown()
            }
        }.start()
        if (!latch.await(12, java.util.concurrent.TimeUnit.SECONDS)) {
            throw IllegalStateException("timed out")
        }
        val thrown = error[0]
        if (thrown != null) throw thrown
        if (!filled) throw IllegalStateException("call returned nothing")
        @Suppress("UNCHECKED_CAST")
        return value[0] as T
    }

    private fun findMovie(): String? {
        val roots = arrayOf(
            File("/mnt/sdcard/MP_ROOT"),
            File("/mnt/sdcard/PRIVATE/M4ROOT"),
            File("/mnt/sdcard/DCIM"),
            context.getExternalFilesDir(null)
        )
        for (root in roots) {
            val found = findMovie(root, 0)
            if (found != null) return found
        }
        return null
    }

    private fun findMovie(dir: File?, depth: Int): String? {
        if (dir == null || depth > 4 || !dir.isDirectory) return null
        val children = dir.listFiles() ?: return null
        for (child in children) {
            val name = child.name.lowercase()
            if (child.isFile && (name.endsWith(".mp4") || name.endsWith(".mts"))) return child.absolutePath
        }
        for (child in children) {
            if (!child.isDirectory) continue
            val found = findMovie(child, depth + 1)
            if (found != null) return found
        }
        return null
    }

    private fun supported(name: String, read: () -> List<String>?, apply: (String) -> Unit) {
        probe(name, Kind.SUPPORTED) {
            val values = read() ?: emptyList()
            if (values.isNotEmpty()) apply(values[0])
            values
        }
    }

    private fun destructive(name: String, block: () -> Any?) {
        if (sony) {
            announce(name)
            lines.add(Line("SKIPPED", name, "not fired"))
            return
        }
        probe(name, Kind.CALL, block)
    }

    private fun probe(name: String, kind: Kind = Kind.CALL, block: () -> Any?) {
        announce(name)
        try {
            val value = callWithTimeout(block)
            if (!sony) {
                record(name, false, "not a Sony camera; " + formatValue(value))
                return
            }
            if (kind == Kind.SUPPORTED && !isSupportedValue(value)) {
                record(name, false, "unsupported; " + formatValue(value))
                return
            }
            record(name, true, formatValue(value))
        } catch (t: Throwable) {
            noteFailure(name, t)
        }
    }

    private fun isSupportedValue(value: Any?): Boolean {
        return when (value) {
            null -> false
            is Boolean -> value
            is Collection<*> -> value.isNotEmpty()
            is Int -> value > 0
            else -> value.toString().isNotEmpty() && value.toString() != "null"
        }
    }

    private fun fail(name: String, error: Throwable) {
        announce(name)
        noteFailure(name, error)
    }

    /** A missing class or method is incompatible. Any other failure was not a real call. */
    private fun noteFailure(name: String, error: Throwable) {
        if (isMissingApi(error)) record(name, false, describe(error))
        else lines.add(Line("SKIPPED", name, describe(error)))
    }

    private fun isMissingApi(error: Throwable): Boolean {
        var current: Throwable? = error
        while (current != null) {
            if (current is NoSuchMethodError || current is NoClassDefFoundError) return true
            val message = current.message
            if (message != null && (message.contains("NoSuchMethodError") || message.contains("NoClassDefFoundError"))) return true
            current = current.cause
        }
        return false
    }

    private fun announce(name: String) {
        done += 1
        onProgress(name, done, total)
    }

    private fun record(name: String, compatible: Boolean, detail: String) {
        lines.add(Line(if (compatible) "COMPATIBLE" else "INCOMPATIBLE", name, detail))
    }

    private fun format(line: Line): String = line.status + "\t" + line.name + "\t" + line.detail

    private fun formatValue(value: Any?): String {
        val text = when (value) {
            null -> "null"
            is Collection<*> -> value.joinToString(",")
            is IntArray -> value.joinToString(",")
            Unit -> "ok"
            else -> value.toString()
        }
        return if (text.length > 160) text.substring(0, 160) + "..." else text
    }

    private fun describe(t: Throwable): String {
        val message = t.message
        return t.javaClass.simpleName + if (message.isNullOrEmpty()) "" else ": " + message
    }
}
