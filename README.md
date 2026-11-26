# OpenMemories: Framework #
Build Android apps for your favorite Sony camera.

## How to use ##
1. Add the framework to your dependencies. Add this to your *build.gradle*:

    ```kotlin
    // settings.gradle.kts
    dependencyResolutionManagement {
        repositories {
            google()
            mavenCentral()
            maven { url = uri("https://jitpack.io") }
        }
    }
    // app/build.gradle.kts
    dependencies {
        implementation("com.github.kungpaogao.OpenMemories-Framework:framework:0.3.2")
        // Sony API stubs, to call com.sony.scalar directly. Never package them: the camera provides the real classes.
        compileOnly("com.github.kungpaogao.OpenMemories-Framework:stubs:0.3.2")
    }
    ```

2. Build your app (see [PMCADemo](https://github.com/ma1co/PMCADemo) for an example)

3. Test it in the emulator

4. Install it on your camera using [PMCA-RE](https://github.com/ma1co/Sony-PMCA-RE)

## Features ##
This is a wrapper framework around the camera OS classes the PlayMemories apps call. Each wrapper uses the Sony implementation on a camera and an Android stand-in everywhere else, so the same app runs in the emulator and on the camera. The choice follows {@code DeviceInfo.isCamera()} ({@code Build.BRAND=sony}, {@code MODEL=ScalarA}, {@code DEVICE=dslr-diadem}).

See the [Javadoc](https://jitpack.io/com/github/ma1co/OpenMemories-Framework/framework/-SNAPSHOT/javadoc/) for more information.

### DeviceInfo ###
Access information about your device (model name, serial number, firmware version, picture sizes, Wi-Fi, GPS, NFC).

```java
import com.github.ma1co.openmemories.framework.DeviceInfo;
String serialNumber = DeviceInfo.getInstance().getSerialNumber();
boolean wifi = DeviceInfo.getInstance().isWifiSupported();
```

### DateTime ###
Get the current date and time.

```java
import com.github.ma1co.openmemories.framework.DateTime;
java.util.Calendar currentTime = DateTime.getInstance().getCurrentTime();
```

### DisplayManager ###
Access information about the device's displays, including viewfinder, HDMI, and 4K output events.

```java
import com.github.ma1co.openmemories.framework.DisplayManager;
DisplayManager displayManager = DisplayManager.create(getApplicationContext());
int displayWidth = displayManager.getActiveDisplayInfo().width;
displayManager.release();
```

### MediaManager ###
Query the images and movies on the memory card, rotate stills, and read free space.

```java
import com.github.ma1co.openmemories.framework.ImageInfo;
import com.github.ma1co.openmemories.framework.MediaManager;
MediaManager mediaManager = MediaManager.create(getApplicationContext());
Cursor cursor = mediaManager.queryNewestImages();
while (cursor.moveToNext()) {
    ImageInfo info = mediaManager.getImageInfo(cursor);
    String filename = info.getFilename();
}
cursor.close();
```

### Camera and stills ###
Open the camera, change Sony parameters, run a capture sequence, and save a JPEG.

```java
import com.github.ma1co.openmemories.framework.Camera;
import com.github.ma1co.openmemories.framework.CameraParameters;
import com.github.ma1co.openmemories.framework.JpegSaver;
import com.github.ma1co.openmemories.framework.StillImage;
Camera camera = Camera.open(null);
CameraParameters parameters = camera.getParameters();
parameters.setDriveMode(CameraParameters.DRIVE_SINGLE);
parameters.apply();
StillImage image = StillImage.decodeFile("/mnt/sdcard/DCIM/100MSDCF/DSC00001.JPG", StillImage.TYPE_SCREEN);
JpegSaver.save(this, image, "external", JpegSaver.QUALITY_FINE);
camera.release();
```

To process a picture the camera stored itself, wait for it in the media database and decode it once the camera is released. The hardware decoder is busy while the shooting camera is open. `Camera.setJpegListener` installs the `CameraEx` JPEG listener only while a listener is set.

```java
mediaManager.setImageAddedListener(new MediaManager.ImageAddedListener() {
    @Override public void onImageAdded(long id) {
        mediaManager.setImageAddedListener(null);
        camera.release();
        StillImage image = StillImage.decodeImage(mediaManager.getImageInfo(id), StillImage.TYPE_FULL);
        // ...
        image.release();
    }
});
camera.takePicture();
```

For manual-focus punch-in, `Camera.setMagnificationListener` reports magnification changes (`onMagnificationChanged(on, ratio, level, x, y)`, ratio times 100) and focus-ring turns (`onFocusRing(rotating, x, y)`). `Camera.getPreviewMagnificationRatio(level)` maps a level to its ratio. `CameraParameters.getSupportedPreviewMagnification()`, `getMfAssist()` and `getMfAssistSeconds()` read the supported levels and the camera's MF Assist menu setting. The emulator supports levels 2 and 3 and sends no focus-ring events.

`Camera.getAutoReviewTime()` and `setAutoReviewTime(time)` read and set the camera's Auto Review (how long a picture stays on screen after it is taken); 0 turns it off, as Sony's processing apps do. Set the old value back before releasing the camera.

`ImageDisplay.createView(context)` creates the view `ImageDisplay.attach` expects on the current device.

Other camera-OS wrappers follow the same pattern: {@code CaptureSequence}, {@code ImageFilters}, {@code FaceFinder}, {@code SignalProcessor}, {@code CameraKeys}, {@code IndicatorLights}, {@code CameraSystem}, {@code CameraNotices}, {@code AudioDevices}, {@code MovieRecorder}, {@code MoviePlayer}, {@code HighlightMovie}, and {@code ImageDisplay}.

The PlayMemories Web API and Leza HTTP stack are libraries shipped inside those apps. They are not part of the camera OS, so this framework does not wrap them.

## Compatibility ##
The sample app in [compatibility](compatibility) calls each framework API and records whether this camera's firmware supports it. A call that returns a result is compatible. A `getSupported*` or `is*Supported` query counts only when that result is a real value. A missing class or method is incompatible. Any other failure, including a timeout, is skipped. Shutter, card format, factory reset, and power-off are not fired.

Build it from that directory and install the debug APK on the camera. The run is logged with the tag `ApiCompat` and written to `COMPAT.TXT` at the root of the memory card. The menu key returns to the Android home screen.

Checked-in results, one file per model, are in [compatibility/reports](compatibility/reports).
