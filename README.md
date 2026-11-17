# OpenMemories: Framework #
Build Android apps for your favorite Sony camera.

## How to use ##
1. Add the framework to your dependencies. Add this to your *build.gradle*:

    ```gradle
    repositories {
        jcenter()
        maven { url "https://jitpack.io" }
    }
    dependencies {
        compile 'com.github.ma1co.OpenMemories-Framework:framework:-SNAPSHOT'

        // If you need direct access to the Sony APIs, you can add this line, too:
        provided 'com.github.ma1co.OpenMemories-Framework:stubs:-SNAPSHOT'
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

Other camera-OS wrappers follow the same pattern: {@code CaptureSequence}, {@code ImageFilters}, {@code FaceFinder}, {@code SignalProcessor}, {@code CameraKeys}, {@code IndicatorLights}, {@code CameraSystem}, {@code CameraNotices}, {@code AudioDevices}, {@code MovieRecorder}, {@code MoviePlayer}, {@code HighlightMovie}, and {@code ImageDisplay}.

The PlayMemories Web API and Leza HTTP stack are libraries shipped inside those apps. They are not part of the camera OS, so this framework does not wrap them.
