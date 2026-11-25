package com.github.ma1co.openmemories.framework;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.provider.MediaStore;
import com.sony.scalar.provider.AvindexStore;

/**
 * Query the images on the memory card.
 * This is the abstract base class.
 * Create an instance using {@link #create}.
 */
public abstract class MediaManager {
    /**
     * MediaManager implementation used on Sony cameras.
     * Instantiate using {@link MediaManager#create}.
     */
    public static class CameraMediaManager extends MediaManager {
        private CameraMediaManager(Context context) {
            super(context, AvindexStore.Images.Media.EXTERNAL_CONTENT_URI, AvindexStore.Images.Media._ID, AvindexStore.Images.Media.CONTENT_CREATED_UTC_DATE_TIME);
        }

        @Override
        public String[] getMediaIds() {
            String[] ids = AvindexStore.getExternalMediaIds();
            return ids == null ? new String[0] : ids;
        }

        @Override
        public String[] getVirtualMediaIds() {
            String[] ids = AvindexStore.getVirtualMediaIds();
            return ids == null ? new String[0] : ids;
        }

        @Override
        public long getAvailableBytes(String mediaId) {
            return AvindexStore.getAvailableSize(mediaId);
        }

        @Override
        public boolean rotateImage(long id, int degrees) {
            if (degrees < -270 || degrees > 270 || degrees % 90 != 0)
                return false;
            if (degrees == 0)
                return true;
            int code;
            if (degrees == 90)
                code = AvindexStore.Images.Media.RELATIVE_ANGLE_PLUS_90;
            else if (degrees == -90)
                code = AvindexStore.Images.Media.RELATIVE_ANGLE_MINUS_90;
            else if (degrees == 180)
                code = AvindexStore.Images.Media.RELATIVE_ANGLE_PLUS_180;
            else if (degrees == -180)
                code = AvindexStore.Images.Media.RELATIVE_ANGLE_MINUS_180;
            else if (degrees == 270)
                code = AvindexStore.Images.Media.RELATIVE_ANGLE_PLUS_270;
            else if (degrees == -270)
                code = AvindexStore.Images.Media.RELATIVE_ANGLE_MINUS_270;
            else
                return false;
            return AvindexStore.Images.Media.rotateImage(context().getContentResolver(), getImageContentUri(), id, code);
        }

        @Override
        public boolean waitForDatabase(String mediaId) {
            return AvindexStore.Images.waitAndUpdateDatabase(context().getContentResolver(), mediaId);
        }

        @Override
        public boolean loadMedia(String mediaId, int contentType) {
            return AvindexStore.loadMedia(mediaId, contentType);
        }

        @Override
        public Cursor queryNewestVideos() {
            String[] ids = getMediaIds();
            String mediaId = ids.length == 0 ? "external" : ids[0];
            return context().getContentResolver().query(
                    AvindexStore.Video.Media.getContentUri(mediaId),
                    new String[] { AvindexStore.Video.Media._ID },
                    null,
                    null,
                    AvindexStore.Video.Media.CONTENT_CREATED_UTC_DATE_TIME + " DESC");
        }

        @Override
        public int getMediaType(String mediaId) {
            com.sony.scalar.media.MediaInfo info = AvindexStore.getMediaInfo(mediaId);
            return info == null ? MEDIA_UNKNOWN : info.getMediaType();
        }

        @Override
        public boolean isEyeFi(String mediaId) {
            com.sony.scalar.media.MediaInfo info = AvindexStore.getMediaInfo(mediaId);
            return info != null && info.isEyeFi();
        }

        private BroadcastReceiver imageWatcherReceiver;
        private BroadcastReceiver availableShotsReceiver;

        @Override
        public int getAvailableShots() {
            String[] ids = getMediaIds();
            if (ids.length == 0)
                return -1;
            return AvindexStore.Images.getAvailableCount(ids[0]);
        }

        @Override
        protected void startWatchingAvailableShots() {
            availableShotsReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context ctxt, Intent intent) {
                    handleAvailableShotsChanged();
                }
            };
            context().registerReceiver(availableShotsReceiver,
                    new IntentFilter(AvindexStore.ACTION_AVINDEX_MEDIA_AVAILABLE_SIZE_CHANGED));
        }

        @Override
        protected void stopWatchingAvailableShots() {
            if (availableShotsReceiver != null) {
                context().unregisterReceiver(availableShotsReceiver);
                availableShotsReceiver = null;
            }
        }

        @Override
        protected void startWatchingImages(Handler handler) {
            // Camera.setStoredImageListener never fires on the ILCE-7R.
            final Handler watchHandler = handler;
            imageWatcherReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context ctxt, Intent intent) {
                    watchHandler.post(new Runnable() {
                        public void run() {
                            handleImageStoreChanged();
                        }
                    });
                }
            };
            context().registerReceiver(imageWatcherReceiver,
                    new IntentFilter(AvindexStore.ACTION_AVINDEX_DATABASE_UPDATED),
                    null,
                    handler);
        }

        @Override
        protected void stopWatchingImages() {
            if (imageWatcherReceiver != null) {
                context().unregisterReceiver(imageWatcherReceiver);
                imageWatcherReceiver = null;
            }
        }
    }

    /**
     * MediaManager implementation used on non-Sony devices.
     * Instantiate using {@link MediaManager#create}.
     */
    public static class AndroidMediaManager extends MediaManager {
        private AndroidMediaManager(Context context) {
            super(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN);
        }

        private ContentObserver imageWatcherObserver;

        @Override
        protected void startWatchingImages(Handler handler) {
            final Handler watchHandler = handler;
            imageWatcherObserver = new ContentObserver(handler) {
                @Override
                public void onChange(boolean selfChange) {
                    watchHandler.post(new Runnable() {
                        public void run() {
                            handleImageStoreChanged();
                        }
                    });
                }
            };
            context().getContentResolver().registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, imageWatcherObserver);
        }

        @Override
        protected void stopWatchingImages() {
            if (imageWatcherObserver != null) {
                context().getContentResolver().unregisterContentObserver(imageWatcherObserver);
                imageWatcherObserver = null;
            }
        }
    }

    public static final int CONTENT_STILL = 1;
    public static final int CONTENT_MP4 = 5;
    public static final int CONTENT_XAVC = 7;
    public static final int MEDIA_UNKNOWN = 0;
    public static final int MEDIA_SD = 1;
    public static final int MEDIA_MEMORY_STICK = 2;

    /**
     * Notified when a newer image appears in the store. {@link #onImageAdded} runs on the watch {@link Handler}
     * (main looper if null), after the broadcast returns; it keeps firing for each newer image until you pass
     * {@code null} to {@link #setImageAddedListener}; pass {@code null} from {@code onPause()}. A background
     * {@code HandlerThread} keeps decode/crop work off the main thread.
     */
    public interface ImageAddedListener {
        void onImageAdded(long id);
    }

    /**
     * Notified when the estimated shots remaining on the card changes. Only fired on a Sony camera.
     */
    public interface AvailableShotsListener {
        void onAvailableShotsChanged(int shots);
    }

    /**
     * Creates a new MediaManager instance
     */
    public static MediaManager create(Context context) {
        if (DeviceInfo.getInstance().isCamera())
            return new CameraMediaManager(context.getApplicationContext());
        else
            return new AndroidMediaManager(context.getApplicationContext());
    }

    private final Context context;
    private final Uri uri;
    private final String idField;
    private final String dateField;
    private final Object imageAddedListenerLock = new Object();
    private ImageAddedListener imageAddedListener;
    private AvailableShotsListener availableShotsListener;
    private long newestImageIdBaseline = -1L;
    private Handler imageWatchHandler;

    private MediaManager(Context context, Uri uri, String idField, String dateField) {
        this.context = context;
        this.uri = uri;
        this.idField = idField;
        this.dateField = dateField;
    }

    /**
     * Returns the Uri for the image ContentProvider
     */
    public Uri getImageContentUri() {
        return uri;
    }

    private Cursor queryImages(String sortOrder) {
        return context.getContentResolver().query(uri, new String[] { idField }, null, null, sortOrder);
    }

    /**
     * Returns a cursor over all images, default sort order. Use {@link #getImageInfo(Cursor)} to get more information
     * about the images.
     */
    public Cursor queryImages() {
        return queryImages(null);
    }

    /**
     * Returns a cursor over all images, newest images coming first. Use {@link #getImageInfo(Cursor)} to get more
     * information about the images.
     */
    public Cursor queryNewestImages() {
        return queryImages(dateField + " DESC");
    }

    /**
     * Returns a cursor over all images, oldest images coming first. Use {@link #getImageInfo(Cursor)} to get more
     * information about the images.
     */
    public Cursor queryOldestImages() {
        return queryImages(dateField + " ASC");
    }

    /**
     * Returns the unique id of the image pointed to by the cursor
     */
    public long getImageId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(idField));
    }

    /**
     * Watch for new images; {@link ImageAddedListener#onImageAdded} runs on the main looper after the broadcast
     * returns. Pass {@code null} from {@code onPause()}: this holds a receiver/observer on the application context.
     */
    public void setImageAddedListener(ImageAddedListener listener) {
        setImageAddedListener(listener, null);
    }

    /**
     * Like {@link #setImageAddedListener(ImageAddedListener)} but runs the store check and
     * {@link ImageAddedListener#onImageAdded} on {@code handler} (main looper if null), after the broadcast returns.
     * A background {@code HandlerThread} keeps decode/crop work off the main thread.
     */
    public void setImageAddedListener(ImageAddedListener listener, Handler handler) {
        stopWatchingImages();
        synchronized (imageAddedListenerLock) {
            imageAddedListener = listener;
            if (listener != null)
                newestImageIdBaseline = queryNewestImageId();
        }
        if (listener != null) {
            imageWatchHandler = handler != null ? handler : new Handler(Looper.getMainLooper());
            startWatchingImages(imageWatchHandler);
        }
    }

    private long queryNewestImageId() {
        Cursor cursor = queryNewestImages();
        try {
            if (cursor == null || !cursor.moveToFirst())
                return -1L;
            return getImageId(cursor);
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    void handleImageStoreChanged() {
        ImageAddedListener listener;
        synchronized (imageAddedListenerLock) {
            listener = imageAddedListener;
            if (listener == null)
                return;
        }
        long id = queryNewestImageId();
        ImageAddedListener listenerToCall = null;
        synchronized (imageAddedListenerLock) {
            if (imageAddedListener != listener)
                return;
            if (id > newestImageIdBaseline) {
                newestImageIdBaseline = id;
                listenerToCall = listener;
            }
        }
        if (listenerToCall != null)
            listenerToCall.onImageAdded(id);
    }

    protected abstract void startWatchingImages(Handler handler);

    protected abstract void stopWatchingImages();

    protected void startWatchingAvailableShots() {
    }

    protected void stopWatchingAvailableShots() {
    }

    void handleAvailableShotsChanged() {
        AvailableShotsListener listener = availableShotsListener;
        if (listener != null)
            listener.onAvailableShotsChanged(getAvailableShots());
    }

    /**
     * Estimated still shots that fit on the first external card.
     * On a camera this calls {@code AvindexStore.Images.getAvailableCount}; on an emulator this returns -1.
     */
    public int getAvailableShots() {
        return -1;
    }

    /**
     * Register for shots-remaining updates. On a camera this listens for
     * {@code ACTION_AVINDEX_MEDIA_AVAILABLE_SIZE_CHANGED}; on an emulator this is a no-op.
     * Pass {@code null} from {@code onPause()}.
     */
    public void setAvailableShotsListener(AvailableShotsListener listener) {
        stopWatchingAvailableShots();
        availableShotsListener = listener;
        if (listener != null)
            startWatchingAvailableShots();
    }

    /**
     * Creates a new {@link ImageInfo} instance for the given image id
     */
    public ImageInfo getImageInfo(long id) {
        return ImageInfo.create(context, uri, id);
    }

    /**
     * Creates a new {@link ImageInfo} instance for the image pointed to by the cursor
     */
    public ImageInfo getImageInfo(Cursor cursor) {
        return getImageInfo(getImageId(cursor));
    }

    protected Context context() {
        return context;
    }

    /** Memory-card ids. {@code external} on an emulator. */
    public String[] getMediaIds() {
        return new String[] { "external" };
    }

    /** Virtual media ids. Empty off a camera. */
    public String[] getVirtualMediaIds() {
        return new String[0];
    }

    /** Free bytes on the given card. */
    public long getAvailableBytes(String mediaId) {
        try {
            StatFs stats = new StatFs(Environment.getExternalStorageDirectory().getPath());
            return (long) stats.getAvailableBlocks() * (long) stats.getBlockSize();
        } catch (RuntimeException ignored) {
            return 0L;
        }
    }

    /**
     * Rotate a stored still by {@code degrees} relative to its current orientation (positive = clockwise).
     * {@code degrees} must be a multiple of 90 between -270 and 270 inclusive.
     */
    public boolean rotateImage(long id, int degrees) {
        if (degrees < -270 || degrees > 270 || degrees % 90 != 0)
            return false;
        if (degrees == 0)
            return true;
        Uri imageUri = ContentUris.withAppendedId(uri, id);
        Cursor cursor = context.getContentResolver().query(
                imageUri,
                new String[] { MediaStore.Images.Media.ORIENTATION },
                null,
                null,
                null);
        try {
            if (cursor == null || !cursor.moveToFirst())
                return false;
            int current = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));
            int next = ((current + degrees) % 360 + 360) % 360;
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.ORIENTATION, Integer.valueOf(next));
            return context.getContentResolver().update(imageUri, values, null, null) > 0;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    /** Block until the image database has caught up with a save. */
    public boolean waitForDatabase(String mediaId) {
        return true;
    }

    /** Ask the camera to index a card. {@code contentType} is one of the {@code CONTENT_*} constants. */
    public boolean loadMedia(String mediaId, int contentType) {
        return true;
    }

    /** Newest movies first. */
    public Cursor queryNewestVideos() {
        return context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Video.Media._ID },
                null,
                null,
                MediaStore.Video.Media.DATE_TAKEN + " DESC");
    }

    /** Id of the video the cursor points at. */
    public long getVideoId(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
    }

    /** {@link #MEDIA_SD}, {@link #MEDIA_MEMORY_STICK}, or {@link #MEDIA_UNKNOWN}. */
    public int getMediaType(String mediaId) {
        return MEDIA_UNKNOWN;
    }

    /** Whether the card is an Eye-Fi card. */
    public boolean isEyeFi(String mediaId) {
        return false;
    }
}
