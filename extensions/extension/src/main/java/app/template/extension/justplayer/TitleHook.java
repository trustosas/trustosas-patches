package app.template.extension.justplayer;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;

@SuppressWarnings("unused")
public class TitleHook {

    /**
     * Replaces the app's getFileName(Context, Uri) for the on-screen title.
     * Uses only Android framework APIs, so it does not depend on the app's obfuscated names.
     * Returns the embedded title tag when present, otherwise the file name.
     */
    public static String resolveTitle(Context context, Uri uri) {
        try {
            String title = readEmbeddedTitle(context, uri);
            if (!TextUtils.isEmpty(title)) return title;
        } catch (Throwable ignored) {
            // Fall through to the file name.
        }
        return fileName(context, uri);
    }

    private static String readEmbeddedTitle(Context context, Uri uri) {
        if (context == null || uri == null) return null;

        // Local sources only: this runs on the main thread, so never touch the network.
        String scheme = uri.getScheme();
        if (!"content".equals(scheme) && !"file".equals(scheme)) return null;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } finally {
            try {
                retriever.release();
            } catch (Throwable ignored) {
            }
        }
    }

    private static String fileName(Context context, Uri uri) {
        if (uri == null) return null;
        try {
            if (context != null && "content".equals(uri.getScheme())) {
                try (Cursor cursor = context.getContentResolver()
                        .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        String name = cursor.getString(0);
                        if (!TextUtils.isEmpty(name)) return name;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        String last = uri.getLastPathSegment();
        return last != null ? last : uri.toString();
    }
}
