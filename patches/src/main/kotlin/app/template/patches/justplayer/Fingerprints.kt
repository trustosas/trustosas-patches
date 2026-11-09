package app.template.patches.justplayer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

/**
 * PlayerActivity.initializePlayer() builds the MediaItem metadata like this:
 *
 *   title = apiTitle != null ? apiTitle : Utils.getFileName(...);   <- first getFileName call
 *   ...
 *   titleView.setText(apiTitle != null ? apiTitle : Utils.getFileName(...));  <- second call
 *
 * Filter 0 matches the FIRST Utils.getFileName call in the method (the MediaItem one),
 * filter 1 is the move-result-object right after it.
 */
object InitializePlayerFingerprint : Fingerprint(
    definingClass = "Lcom/brouken/player/PlayerActivity;",
    name = "initializePlayer",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/brouken/player/Utils;",
            name = "getFileName"
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately())
    )
)

/**
 * PlayerActivity.PlayerListener.onMediaMetadataChanged(MediaMetadata)
 * In the release build this is a synthetic stub that only calls the Player.Listener default.
 */
object OnMediaMetadataChangedFingerprint : Fingerprint(
    definingClass = "Lcom/brouken/player/PlayerActivity\$PlayerListener;",
    name = "onMediaMetadataChanged",
    returnType = "V",
    parameters = listOf("Landroidx/media3/common/MediaMetadata;")
)
