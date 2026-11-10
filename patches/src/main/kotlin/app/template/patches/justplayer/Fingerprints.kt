package app.template.patches.justplayer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

/*
 * Just Player 0.218 is R8-obfuscated, so these names only match this version:
 *   td0   = Utils            (td0.D  = Utils.getFileName(Context, Uri))
 *   fx0   = merged Player.Listener (fx0.j = onMediaMetadataChanged, fx0.k = owner object)
 *   tl0   = MediaMetadata    (tl0.a  = title)
 *   PlayerActivity.z = titleView
 * When the app updates, re-check these names (see the hook in TitleHook.java).
 */

/**
 * PlayerActivity.initializePlayer() (obfuscated name "h") calls getFileName twice:
 *   1st: MediaItem title  ->  title = apiTitle != null ? apiTitle : getFileName(...)
 *   2nd: titleView.setText(...)
 * Filter 0 matches the FIRST call, filter 1 is the move-result-object right after it.
 * The method name is obfuscated, so it is matched by its contents only.
 */
object InitializePlayerFingerprint : Fingerprint(
    definingClass = "Lcom/brouken/player/PlayerActivity;",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Ltd0;",
            name = "D"
        ),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately())
    )
)

/**
 * Player.Listener.onMediaMetadataChanged(MediaMetadata) in the merged listener class.
 * In the release build this is only: iget-byte v0, v0, fx0->j ; return-void
 */
object OnMediaMetadataChangedFingerprint : Fingerprint(
    definingClass = "Lfx0;",
    name = "j",
    returnType = "V",
    parameters = listOf("Ltl0;")
)
