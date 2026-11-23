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
        public boolean rotateImage(long id, int orientation) {
            return AvindexStore.Images.Media.rotateImage(context().getContentResolver(), getImageContentUri(), id, orientation);
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

        @Override
        protected void startWatchingImages() {
            // Camera.setStoredImageListener never fires on the ILCE-7R.
            imageWatcherReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context ctxt, Intent intent) {
                    handleImageStoreChanged();
                }
            };
            context().registerReceiver(imageWatcherReceiver,
                    new IntentFilter(AvindexStore.ACTION_AVINDEX_DATABASE_UPDATED));
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
        protected void startWatchingImages() {
            imageWatcherObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    handleImageStoreChanged();
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
     * Notified when a newer image appears in the store. {@link #onImageAdded} runs on the main thread and keeps
     * firing for each newer image until you pass {@code null} to {@link #setImageAddedListener}; pass {@code null}
     * from {@code onPause()}.
     */
    public interface ImageAddedListener {
        void onImageAdded(long id);
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
    private ImageAddedListener imageAddedListener;
    private long newestImageIdBaseline = -1L;

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
     * Watch for new images; {@link ImageAddedListener#onImageAdded} runs on the main thread for each newer id until
     * you pass {@code null}. Pass {@code null} from {@code onPause()}: this holds a receiver/observer on the
     * application context.
     */
    public void setImageAddedListener(ImageAddedListener listener) {
        stopWatchingImages();
        imageAddedListener = listener;
        if (listener != null) {
            newestImageIdBaseline = queryNewestImageId();
            startWatchingImages();
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
        if (imageAddedListener == null)
            return;
        long id = queryNewestImageId();
        if (id > newestImageIdBaseline) {
            newestImageIdBaseline = id;
            imageAddedListener.onImageAdded(id);
        }
    }

    protected abstract void startWatchingImages();

    protected abstract void stopWatchingImages();

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

    /** Rotate a stored still. {@code orientation} is degrees. */
    public boolean rotateImage(long id, int orientation) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.ORIENTATION, Integer.valueOf(orientation));
        return context.getContentResolver().update(ContentUris.withAppendedId(uri, id), values, null, null) > 0;
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
