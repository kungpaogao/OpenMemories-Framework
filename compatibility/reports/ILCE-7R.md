## Summary

| Status | Count |
|---|---|
| Compatible | 200 |
| Incompatible | 47 |
| Skipped | 40 |

## DeviceInfo

| API | Status | Result |
|---|---|---|
| DeviceInfo.isCamera | Compatible | true |
| DeviceInfo.getBrand | Compatible | sony |
| DeviceInfo.getModel | Compatible | ILCE-7R |
| DeviceInfo.getCategory | Compatible | E_MOUNT_CAMERA |
| DeviceInfo.getFirmwareVersion | Compatible | 3.20 |
| DeviceInfo.getHardwareVersion | Compatible | 2 |
| DeviceInfo.getApiVersion | Compatible | 3 |
| DeviceInfo.getAndroidVersion | Compatible | 2.3.7 |
| DeviceInfo.getAndroidSdkVersion | Compatible | 10 |
| DeviceInfo.getPlatformVersion | Compatible | 2.3 |
| DeviceInfo.isWifiSupported | Compatible | true |
| DeviceInfo.hasMechanicalShutter | Incompatible | unsupported; false |
| DeviceInfo.getSensorMegapixels | Compatible | 36 |
| DeviceInfo.getSupportedPictureSizes | Compatible | non-empty list |
| DeviceInfo.getProperty | Compatible | ILCE-7R |
| DeviceInfo.getIntProperty | Compatible | 36 |
| DeviceInfo.getWifiProductCode | Compatible | E0 |

## DateTime

| API | Status | Result |
|---|---|---|
| DateTime.getCurrentTime | Compatible | 1795005104506 |
| DateTime.getGmtOffsetMinutes | Compatible | -300 |

## DisplayManager

| API | Status | Result |
|---|---|---|
| DisplayManager.create | Compatible | created |
| DisplayManager.getActiveDisplay | Compatible | SCREEN |
| DisplayManager.getActiveDisplayInfo | Compatible | 640x480 |
| DisplayManager.getDisplayInfo | Compatible | 640 |
| DisplayManager.getFrameBufferInfo | Compatible | 640 |
| DisplayManager.setActiveDisplay | Compatible | ok |
| DisplayManager.setColorDepth | Compatible | ok |
| DisplayManager.addListener | Compatible | ok |
| DisplayManager.addEventListener | Compatible | ok |
| DisplayManager.startQfhdOutput | Incompatible | NoSuchMethodError: hardware.avio.DisplayManager.startQfhdFormatOutput |
| DisplayManager.stopQfhdOutput | Incompatible | NoSuchMethodError: hardware.avio.DisplayManager.stopQfhdFormatOutput |

## MediaManager

| API | Status | Result |
|---|---|---|
| MediaManager.create | Compatible | created |
| MediaManager.getMediaIds | Compatible | 1000 |
| MediaManager.getVirtualMediaIds | Compatible | 4000 |
| MediaManager.getAvailableBytes | Compatible | 1 |
| MediaManager.getMediaType | Compatible | 1 |
| MediaManager.isEyeFi | Compatible | false |
| MediaManager.queryNewestImages | Compatible | 839 |
| MediaManager.queryNewestVideos | Incompatible | NoClassDefFoundError: provider.AvindexStore$Video$Media |
| MediaManager.loadMedia | Compatible | false |
| MediaManager.waitForDatabase | Compatible | false |
| MediaManager.rotateImage | Compatible | false |

## CameraSystem

| API | Status | Result |
|---|---|---|
| CameraSystem.notifyRunning | Compatible | ok |
| CameraSystem.setAutoPowerOffEnabled | Compatible | ok |
| CameraSystem.open | Compatible | open |
| CameraSystem.getAutoPowerOffSeconds | Incompatible | NoSuchMethodError: sysutil.didep.Settings.getAutoPowerOffTime |
| CameraSystem.setAutoPowerOffSeconds | Incompatible | NoSuchMethodError: sysutil.didep.Settings.setAutoPowerOffTime |
| CameraSystem.getSupportedAutoPowerOffTimes | Incompatible | NoSuchMethodError: sysutil.didep.Settings.getSupportedAutoPowerOffTimes |
| CameraSystem.getAutoRotate | Compatible | 0 |
| CameraSystem.getDialSetting | Compatible | 0 |
| CameraSystem.getGridLine | Compatible | 0 |
| CameraSystem.getTargetMediaId | Incompatible | NoSuchMethodError: sysutil.didep.Settings.getTargetMediaId |
| CameraSystem.setKeyLock | Compatible | ok |
| CameraSystem.isKeyLocked | Compatible | false |
| CameraSystem.isSystemReady | Compatible | true |
| CameraSystem.getBatteryPercent | Compatible | 85 |
| CameraSystem.setBatteryListener | Incompatible | NoClassDefFoundError: sysutil.didep.Power |
| CameraSystem.getTemperatureStatus | Compatible | 1 |
| CameraSystem.setTemperatureListener | Skipped | RuntimeException: Can't create handler inside thread that has not called Looper.prepare() |
| CameraSystem.getGpsPower | Incompatible | NoClassDefFoundError: sysutil.didep.Gps |
| CameraSystem.getGpsState | Compatible | 1 |
| CameraSystem.startGps | Incompatible | NoClassDefFoundError: sysutil.didep.Gps |
| CameraSystem.stopGps | Incompatible | NoClassDefFoundError: sysutil.didep.Gps |
| CameraSystem.isNfcEnabled | Incompatible | NoClassDefFoundError: sysutil.didep.Nfc |
| CameraSystem.startNfc | Incompatible | NoClassDefFoundError: sysutil.didep.Nfc |
| CameraSystem.stopNfc | Incompatible | NoClassDefFoundError: sysutil.didep.Nfc |
| CameraSystem.isBluetoothOn | Incompatible | NoClassDefFoundError: sysutil.didep.Bluetooth |
| CameraSystem.isBluetoothConnected | Incompatible | NoClassDefFoundError: sysutil.didep.Bluetooth |
| CameraSystem.log | Compatible | ok |
| CameraSystem.formatCard | Skipped | not fired |
| CameraSystem.recoverCard | Skipped | not fired |
| CameraSystem.resetAll | Skipped | not fired |
| CameraSystem.requestPowerOff | Skipped | not fired |

## CameraKeys

| API | Status | Result |
|---|---|---|
| CameraKeys.fromKeyEvent | Compatible | 0 |
| CameraKeys.getStatus | Compatible | 0 |

## IndicatorLights

| API | Status | Result |
|---|---|---|
| IndicatorLights.initialize | Incompatible | NoSuchMethodError: hardware.indicator.Light.initialize |
| IndicatorLights.set | Compatible | ok |
| IndicatorLights.setPattern | Compatible | ok |
| IndicatorLights.setSubLcd | Incompatible | NoClassDefFoundError: hardware.indicator.SubLCD |
| IndicatorLights.hdmiOneTouchPlay | Compatible | ok |
| IndicatorLights.release | Incompatible | NoSuchMethodError: hardware.indicator.Light.release |

## CameraNotices

| API | Status | Result |
|---|---|---|
| CameraNotices.setEnabled | Skipped | RuntimeException: Caution callback is not set. but EnableCaution is called |
| CameraNotices.setFactor | Skipped | RuntimeException: Caution callback is not set. but SetFactor is called |
| CameraNotices.setTrigger | Skipped | RuntimeException: Caution callback is not set. but SetTrigger is called |

## Camera

| API | Status | Result |
|---|---|---|
| Camera.open | Compatible | open |
| Camera.setPreviewDisplay | Compatible | ok |
| Camera.startPreview | Compatible | ok |
| Camera.getNormalCamera | Compatible | true |
| Camera.getLens | Compatible | TTARTISAN 40mm F2.0 |
| Camera.getExternalFlash | Compatible | false |
| Camera.getPreviewColor | Compatible | 160 |
| Camera.setColorSelect | Compatible | ok |
| Camera.createGammaCurve | Compatible | gamma |
| Camera.setPreviewMagnification | Compatible | ok |
| Camera.stopPreviewMagnification | Compatible | ok |
| Camera.startZoom | Compatible | ok |
| Camera.stopZoom | Compatible | ok |
| Camera.incrementShutterSpeed | Compatible | ok |
| Camera.decrementShutterSpeed | Compatible | ok |
| Camera.adjustShutterSpeed | Compatible | ok |
| Camera.incrementAperture | Compatible | ok |
| Camera.decrementAperture | Compatible | ok |
| Camera.adjustAperture | Compatible | ok |
| Camera.shiftFocus | Skipped | IllegalStateException: timed out |
| Camera.startOneShotFocusDrive | Compatible | ok |
| Camera.startFocusHold | Incompatible | NoSuchMethodError: hardware.CameraEx.startFocusHold |
| Camera.stopFocusHold | Incompatible | NoSuchMethodError: hardware.CameraEx.stopFocusHold |
| Camera.startFaceDetection | Skipped | RuntimeException: FaceDetection() failed |
| Camera.stopFaceDetection | Skipped | RuntimeException: FaceDetection() failed |
| Camera.cancelAutoFocus | Compatible | ok |
| Camera.cancelTakePicture | Compatible | ok |
| Camera.cancelExposure | Compatible | ok |
| Camera.stopDirectShutter | Compatible | ok |
| Camera.cancelSelfTimerShutter | Compatible | false |
| Camera.takePicture | Skipped | not fired |
| Camera.burstableTakePicture | Skipped | not fired |
| Camera.startDirectShutter | Skipped | not fired |
| Camera.autoFocus | Skipped | not fired |
| Camera.startSelfTimerShutter | Skipped | not fired |
| Camera.stopPreview | Compatible | ok |
| Camera.setShutterListener | Compatible | ok |
| Camera.setCaptureStatusListener | Compatible | ok |
| Camera.setJpegListener | Compatible | ok |
| Camera.setAutoFocusListener | Compatible | ok |
| Camera.setFocusListener | Compatible | ok |
| Camera.setApertureListener | Compatible | ok |
| Camera.setShutterSpeedListener | Compatible | ok |
| Camera.setZoomListener | Compatible | ok |
| Camera.setFaceListener | Compatible | ok |
| Camera.setStoredImageListener | Compatible | ok |
| Camera.getParameters | Compatible | parameters |
| Camera.release | Skipped | IllegalStateException: timed out |

## CameraParameters

| API | Status | Result |
|---|---|---|
| CameraParameters.getSupportedWhiteBalance | Compatible | auto,daylight,shade,cloudy-daylight,fluorescent-coolwhite,fluorescent-daywhite,fluorescent-daylight,warm-fluorescent,incandescent,flash,custom,color-temp,underw... |
| CameraParameters.getSupportedFlashModes | Compatible | auto,on,off |
| CameraParameters.getSupportedFocusModes | Compatible | manual,auto,dmf |
| CameraParameters.getSupportedSceneModes | Compatible | program-auto,aperture-priority,shutter-speed,manual-exposure,auto,portrait,landscape,night,night-portrait,sunset,sports,hand-held-twilight,anti-motion-blur,macr... |
| CameraParameters.getSupportedIso | Compatible | 0,50,64,80,100,125,160,200,250,320,400,500,640,800,1000,1250,1600,2000,2500,3200,4000,5000,6400,8000,10000,12800,16000,20000,25600 |
| CameraParameters.getSupportedDriveModes | Compatible | single,burst,bracket,speed-prior-burst |
| CameraParameters.getSupportedPictureEffects | Compatible | off,toy-camera,pop-color,posterization,retro-photo,soft-high-key,part-color,rough-mono,soft-focus,hdr-art,richtone-mono,miniature,illust,watercolor |
| CameraParameters.getSupportedImageSizes | Compatible | small,midium,large |
| CameraParameters.getSupportedAspectRatios | Compatible | 3:2,16:9 |
| CameraParameters.getSupportedMeteringModes | Compatible | multi,center-weighted,center-spot |
| CameraParameters.getSupportedColorDepthTypes | Compatible | red,green,blue,cyan,magenta,yellow |
| CameraParameters.getSupportedLensCorrections | Compatible | shading-w,shading-wm,shading-cr,shading-cb,shading-cm,chroma-r,chroma-b,distotion,distotion-m |
| CameraParameters.isAutoWhiteBalanceLockSupported | Compatible | true |
| CameraParameters.isRemoteControlModeSupported | Compatible | true |
| CameraParameters.isRgbMatrixSupported | Compatible | true |
| CameraParameters.isSharpnessGainModeSupported | Compatible | true |
| CameraParameters.isWhiteBalanceShiftModeSupported | Compatible | true |
| CameraParameters.isExtendedGammaSupported | Compatible | true |
| CameraParameters.isExposureShiftSupported | Compatible | true |
| CameraParameters.getAperture | Compatible | 200 |
| CameraParameters.getShutterSpeed | Compatible | 1/60 |
| CameraParameters.getExposureCompensation | Compatible | 0 |
| CameraParameters.setContrast | Compatible | ok |
| CameraParameters.setSaturation | Compatible | ok |
| CameraParameters.setSharpness | Compatible | ok |
| CameraParameters.setAutoExposureLock | Compatible | ok |
| CameraParameters.setAutoWhiteBalanceLock | Compatible | ok |
| CameraParameters.setSilentShutter | Incompatible | NoSuchMethodError: hardware.CameraEx$ParametersModifier.setSilentShutterMode |
| CameraParameters.setColorTemperature | Compatible | ok |
| CameraParameters.apply | Skipped | IllegalStateException: timed out |
| CameraParameters.getIso | Compatible | 0 |
| CameraParameters.getDriveMode | Compatible | single |
| CameraParameters.getPictureEffect | Compatible | off |
| CameraParameters.getImageSize | Compatible | small |
| CameraParameters.getAspectRatio | Compatible | 3:2 |
| CameraParameters.getMeteringMode | Compatible | multi |
| CameraParameters.getDroMode | Compatible | auto |
| CameraParameters.setDroMode | Compatible | ok |
| CameraParameters.getContrast | Compatible | 0 |
| CameraParameters.getSaturation | Compatible | 0 |
| CameraParameters.getSharpness | Compatible | 0 |
| CameraParameters.getFaceDetectionMode | Compatible | auto |
| CameraParameters.setFaceDetectionMode | Compatible | ok |
| CameraParameters.getAutoFocusMode | Compatible | af-s |
| CameraParameters.setAutoFocusMode | Compatible | ok |
| CameraParameters.getAntiHandBlurMode | Compatible | off |
| CameraParameters.setAntiHandBlurMode | Compatible | ok |
| CameraParameters.getRemoteControlMode | Compatible | false |
| CameraParameters.setRemoteControlMode | Compatible | ok |
| CameraParameters.getRgbMatrix | Compatible | 0 |
| CameraParameters.getLongExposureNoiseReduction | Compatible | true |
| CameraParameters.getColorSelectMode | Compatible | off |
| CameraParameters.getSharpnessGainMode | Compatible | false |
| CameraParameters.setSharpnessGainMode | Compatible | ok |
| CameraParameters.getSharpnessGain | Compatible | 0 |
| CameraParameters.setSharpnessGain | Compatible | ok |
| CameraParameters.getWhiteBalanceShiftMode | Compatible | false |
| CameraParameters.setWhiteBalanceShiftMode | Compatible | ok |
| CameraParameters.getWhiteBalanceShiftAmberBlue | Compatible | 0 |
| CameraParameters.setWhiteBalanceShiftAmberBlue | Compatible | ok |
| CameraParameters.getWhiteBalanceShiftGreenMagenta | Compatible | 0 |
| CameraParameters.setWhiteBalanceShiftGreenMagenta | Compatible | ok |
| CameraParameters.getExposureShift | Compatible | 0 |
| CameraParameters.setExposureShift | Compatible | ok |
| CameraParameters.getFocusAreaMode | Compatible | center |
| CameraParameters.getFocusPointIndex | Compatible | 1 |
| CameraParameters.getLensCorrection | Compatible | true |
| CameraParameters.setLensCorrection | Compatible | ok |
| CameraParameters.getColorTemperature | Compatible | 5500 |
| CameraParameters.getJpegQuality | Compatible | 95 |
| CameraParameters.setJpegQuality | Compatible | ok |
| CameraParameters.getPictureSize | Compatible | 7360x4912 |
| CameraParameters.getMinExposureCompensation | Compatible | -15 |
| CameraParameters.getMaxExposureCompensation | Compatible | 15 |

## CaptureSequence

| API | Status | Result |
|---|---|---|
| CaptureSequence.open | Skipped | RuntimeException: setShutterSequenceCallbackMode failed |
| CaptureSequence.setOptions | Skipped | RuntimeException: setShutterSequenceCallbackMode failed |
| CaptureSequence.setShutterListener | Skipped | RuntimeException: setShutterSequenceCallbackMode failed |
| CaptureSequence.setPreviewProcessor | Skipped | RuntimeException: setShutterSequenceCallbackMode failed |
| CaptureSequence.startPreview | Skipped | RuntimeException: setShutterSequenceCallbackMode failed |
| CaptureSequence.stopPreview | Skipped | RuntimeException: setShutterSequenceCallbackMode failed |
| CaptureSequence.develop | Skipped | RuntimeException: setShutterSequenceCallbackMode failed |

## SignalProcessor

| API | Status | Result |
|---|---|---|
| SignalProcessor.create | Compatible | created |
| SignalProcessor.toAxiAddress | Compatible | 0 |
| SignalProcessor.setAllocationPolicy | Compatible | ok |
| SignalProcessor.loadMemoryMap | Skipped | IllegalArgumentException: Invalid Param |
| SignalProcessor.create | Compatible | created |
| SignalProcessor.isHardware | Compatible | true |
| SignalProcessor.setProgram | Skipped | RuntimeException: setProgram failed. |
| SignalProcessor.getProgram | Compatible | probe |
| SignalProcessor.getIntProperty | Compatible | 0 |
| SignalProcessor.createBuffer | Compatible | 16 |
| SignalProcessor.createImage | Compatible | null |
| SignalProcessor.setArgument | Compatible | ok |
| SignalProcessor.getArgument | Compatible | 1 |
| SignalProcessor.execute | Incompatible | unsupported; false |
| SignalProcessor.clearProgram | Compatible | ok |

## StillImage

| API | Status | Result |
|---|---|---|
| StillImage.fromBitmap | Compatible | 16 |
| StillImage.getWidth | Compatible | 16 |
| StillImage.getHeight | Compatible | 16 |
| StillImage.isValid | Compatible | true |
| StillImage.toJpeg | Compatible | 631 |
| StillImage.decodeFile | Compatible | 0 |
| StillImage.fromJpeg | Compatible | false |

## ImageFilters

| API | Status | Result |
|---|---|---|
| ImageFilters.scale | Compatible | 8 |
| ImageFilters.crop | Compatible | 8 |
| ImageFilters.rotate | Compatible | 16 |
| ImageFilters.miniature | Compatible | 16 |
| ImageFilters.faceNoiseReduction | Compatible | 16 |
| ImageFilters.contrast | Compatible | 16 |
| ImageFilters.redEye | Compatible | 16 |
| ImageFilters.softFocus | Compatible | 16 |
| ImageFilters.superResolution | Compatible | 32 |

## FaceFinder

| API | Status | Result |
|---|---|---|
| FaceFinder.find | Compatible | 0 |

## JpegSaver

| API | Status | Result |
|---|---|---|
| JpegSaver.save | Compatible | null |

## ImageDisplay

| API | Status | Result |
|---|---|---|
| ImageDisplay.attach | Skipped | IllegalArgumentException: Expected an OptimizedImageView |

## AudioDevices

| API | Status | Result |
|---|---|---|
| AudioDevices.create | Compatible | created |
| AudioDevices.setMicrophoneEnabled | Skipped | IllegalStateException |
| AudioDevices.isMicrophoneEnabled | Compatible | false |
| AudioDevices.setSoundVolume | Skipped | IllegalStateException |
| AudioDevices.getSoundVolume | Skipped | IllegalStateException |
| AudioDevices.setOutputBeep | Skipped | IllegalStateException |
| AudioDevices.getOutputBeep | Compatible | off |
| AudioDevices.setWindNoiseReduction | Skipped | IllegalStateException |
| AudioDevices.isWindNoiseReduction | Compatible | false |
| AudioDevices.read | Incompatible | NoClassDefFoundError: media.AudioRecord |
| AudioDevices.startRecording | Skipped | not fired |
| AudioDevices.stopRecording | Incompatible | NoClassDefFoundError: media.AudioRecord |
| AudioDevices.play | Skipped | not fired |
| AudioDevices.write | Incompatible | NoClassDefFoundError: media.AudioTrack |
| AudioDevices.stopPlayback | Incompatible | NoClassDefFoundError: media.AudioTrack |

## MovieRecorder

| API | Status | Result |
|---|---|---|
| MovieRecorder.create | Compatible | created |
| MovieRecorder.setOutputMedia | Skipped | NumberFormatException: unable to parse 'external' as integer |
| MovieRecorder.setOutputFile | Compatible | ok |
| MovieRecorder.prepare | Skipped | IllegalStateException |
| MovieRecorder.start | Skipped | not fired |
| MovieRecorder.stop | Skipped | IllegalStateException |

## MoviePlayer

| API | Status | Result |
|---|---|---|
| MoviePlayer.create | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.setDataSource | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.prepare | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.setPlaybackSpeed | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.setDisplayMode | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.getDuration | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.getCurrentPosition | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.isPlaying | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.seekTo | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.stepForward | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.stepRewind | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.start | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.pause | Incompatible | NoClassDefFoundError: media.MediaPlayer |
| MoviePlayer.stop | Incompatible | NoClassDefFoundError: media.MediaPlayer |

## HighlightMovie

| API | Status | Result |
|---|---|---|
| HighlightMovie.create | Incompatible | NoClassDefFoundError: media.HighlightMovieMaker |
| HighlightMovie.isSupported | Incompatible | NoClassDefFoundError: media.HighlightMovieMaker |
| HighlightMovie.setBackgroundMusic | Incompatible | NoClassDefFoundError: media.HighlightMovieMaker |
| HighlightMovie.setLength | Incompatible | NoClassDefFoundError: media.HighlightMovieMaker |
| HighlightMovie.prepare | Incompatible | NoClassDefFoundError: media.HighlightMovieMaker |
| HighlightMovie.execute | Skipped | not fired |
