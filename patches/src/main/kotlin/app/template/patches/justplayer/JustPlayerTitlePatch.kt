package app.template.patches.justplayer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.patch
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val EXTENSION_CLASS = "Lapp/template/extension/justplayer/TitleHook;"

private val COMPATIBILITY_JUST_PLAYER = Compatibility(
    name = "Just (Video) Player",
    packageName = "com.brouken.player",
    apkFileType = ApkFileType.APK,
    appIconColor = 0x1A1A1A, // Cosmetic only, change to taste.
    targets = listOf(
        // Class/field names are R8-obfuscated and only valid for this exact version.
        AppTarget(version = "0.218", isExperimental = true)
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
        // 1. Stop forcing the filename as the MediaItem title.
        //    Original: title = apiTitle != null ? apiTitle : Utils.getFileName(...)
        //    Patched:  the getFileName result is replaced with null, and the existing
        //    `if (title != null)` check then skips setting MediaMetadata entirely.
        InitializePlayerFingerprint.let {
            val moveResult = it.instructionMatches[1]
            val register = moveResult.getInstruction<OneRegisterInstruction>().registerA

            it.method.addInstructions(moveResult.index + 1, "const/4 v$register, 0x0")
        }

        // 2. Update the on-screen title when the real metadata arrives.
        //    The release stub is just `iget-byte v0, v0, ...; return-void` (2 registers: p0 = listener,
        //    p1 = MediaMetadata). Nothing is clobbered, so the hook call is simply put in front of it.
        OnMediaMetadataChangedFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p0, p1}, $EXTENSION_CLASS->onMediaMetadataChanged(Ljava/lang/Object;Ljava/lang/Object;)V
            """
        )
    }
}
