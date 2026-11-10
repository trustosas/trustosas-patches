package app.template.extension.justplayer;

import android.text.TextUtils;
import android.widget.TextView;

import java.lang.reflect.Field;

@SuppressWarnings("unused")
public class TitleHook {
    // Just Player 0.218 (R8-obfuscated). Update these when the app updates.
    private static final String ACTIVITY_CLASS = "com.brouken.player.PlayerActivity";
    private static final String LISTENER_OWNER_FIELD = "k"; // fx0.k  -> owning object
    private static final String METADATA_TITLE_FIELD = "a"; // tl0.a  -> MediaMetadata.title
    private static final String TITLE_VIEW_FIELD = "z";     // PlayerActivity.z -> titleView

    private static Field ownerField;
    private static Field titleField;
    private static Field titleViewField;

    /**
     * Injected at the start of the merged Player.Listener's onMediaMetadataChanged.
     * The listener class is shared by several listeners, so first check the owner is PlayerActivity.
     * Both parameters are Object so the extension needs no compile-time dependency on the app or media3.
     */
    public static void onMediaMetadataChanged(Object listener, Object mediaMetadata) {
        try {
            if (listener == null || mediaMetadata == null) return;

            if (ownerField == null) ownerField = accessible(listener.getClass(), LISTENER_OWNER_FIELD);
            Object activity = ownerField.get(listener);
            if (activity == null || !ACTIVITY_CLASS.equals(activity.getClass().getName())) return;

            if (titleField == null) titleField = accessible(mediaMetadata.getClass(), METADATA_TITLE_FIELD);
            Object title = titleField.get(mediaMetadata);
            if (!(title instanceof CharSequence) || TextUtils.isEmpty((CharSequence) title)) return;

            if (titleViewField == null) titleViewField = accessible(activity.getClass(), TITLE_VIEW_FIELD);
            Object view = titleViewField.get(activity);
            if (view instanceof TextView) {
                ((TextView) view).setText((CharSequence) title);
            }
        } catch (Throwable ignored) {
            // Never crash playback over a title.
        }
    }

    private static Field accessible(Class<?> cls, String name) throws NoSuchFieldException {
        Field field = cls.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
