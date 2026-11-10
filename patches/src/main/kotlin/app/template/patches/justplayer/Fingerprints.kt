package app.template.patches.justplayer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

/**
 * Just Player is R8-obfuscated and names change between releases, so nothing here depends on
 * obfuscated names. Only things that never get renamed are used:
 *  - PlayerActivity (an Android manifest component, so its name is kept)
 *  - Android framework types (Context, Uri, String)
 *
 * PlayerActivity.initializePlayer() calls the app's getFileName(Context, Uri): String twice:
 *   1st: MediaItem title  ->  title = apiTitle != null ? apiTitle : getFileName(...)
 *   2nd: titleView.setText(apiTitle != null ? apiTitle : getFileName(...))
 * instructionMatches: [0] 1st call, [1] its move-result, [2] 2nd call, [3] its move-result.
 */
private fun fileNameCall() = methodCall(
    parameters = listOf("Landroid/content/Context;", "Landroid/net/Uri;"),
    returnType = "Ljava/lang/String;",
    opcode = Opcode.INVOKE_STATIC
)

object InitializePlayerFingerprint : Fingerprint(
    definingClass = "Lcom/brouken/player/PlayerActivity;",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        fileNameCall(),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately()),
        fileNameCall(),
        opcode(Opcode.MOVE_RESULT_OBJECT, MatchAfterImmediately())
    )
)
