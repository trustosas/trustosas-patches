package app.template.patches.justplayer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.patch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS = "Lapp/template/extension/justplayer/TitleHook;"

private val COMPATIBILITY_JUST_PLAYER = Compatibility(
    name = "Just (Video) Player",
    packageName = "com.brouken.player",
    apkFileType = ApkFileType.APK,
    appIconColor = 0x1A1A1A, // Cosmetic only, change to taste.
    targets = listOf(
        // null = any version, "lucky go" until upstream refactors this code.
        AppTarget(version = null, isExperimental = true)
    )
)

@Suppress("unused")
val useMediaTitleTagPatch = patch(
    name = "Use title tag instead of filename",
    description = "Reads the title from the media's embedded tags instead of forcing the filename, " +
        "so media session consumers such as scrobblers get the real track title.",
    default = true
) {
    compatibleWith(COMPATIBILITY_JUST_PLAYER)

    extendWith("extensions/extension.mpe")

    execute {
        // 1. Show the real title on screen too. Original: titleView.setText(... getFileName(ctx, uri))
        //    The call is redirected to the extension, which reads the embedded title tag and falls
        //    back to the filename. Same signature, so the registers and move-result stay untouched.
        InitializePlayerFingerprint.let {
            val call = it.instructionMatches[2]
            val args = call.getInstruction<FiveRegisterInstruction>()

            it.method.replaceInstruction(
                call.index,
                "invoke-static {v${args.registerC}, v${args.registerD}}, " +
                    "$EXTENSION_CLASS->resolveTitle(Landroid/content/Context;Landroid/net/Uri;)Ljava/lang/String;"
            )
        }

        // 2. Stop forcing the filename as the MediaItem title.
        //    (Runs after step 1 on purpose: this inserts an instruction and would shift the indices.)
        //    Original: title = apiTitle != null ? apiTitle : Utils.getFileName(...)
        //    Patched:  the getFileName result is replaced with null, and the existing
        //    `if (title != null)` check then skips setting MediaMetadata entirely.
        InitializePlayerFingerprint.let {
            val moveResult = it.instructionMatches[1]
            val register = moveResult.getInstruction<OneRegisterInstruction>().registerA

            it.method.addInstructions(moveResult.index + 1, "const/4 v$register, 0x0")
        }
    }
}
