package app.template.extension.justplayer;

import android.text.TextUtils;
import android.widget.TextView;

import java.lang.reflect.Field;

@SuppressWarnings("unused")
public class TitleHook {
    private static Field titleViewField;

    /**
     * Injected into PlayerActivity.PlayerListener.onMediaMetadataChanged.
     *
     * Both parameters are typed Object so the extension needs no compile-time dependency
     * on the app or on media3. Mirrors the original source patch:
     * if the metadata has a non-empty title, show it in the overlay title view.
     */
    public static void onMediaMetadataChanged(Object activity, Object mediaMetadata) {
        try {
            if (activity == null || mediaMetadata == null) return;

            Object title = mediaMetadata.getClass().getField("title").get(mediaMetadata);
            if (!(title instanceof CharSequence) || TextUtils.isEmpty((CharSequence) title)) return;

            if (titleViewField == null) {
                Field field = activity.getClass().getDeclaredField("titleView");
                field.setAccessible(true);
                titleViewField = field;
            }

            Object view = titleViewField.get(activity);
            if (view instanceof TextView) {
                ((TextView) view).setText((CharSequence) title);
            }
        } catch (Throwable ignored) {
            // Never crash playback over a title.
        }
    }
}
