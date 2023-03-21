package site.duqian.plugin.svga.svga;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SvgaFileType extends LanguageFileType {

    static final SvgaFileType INSTANCE = new SvgaFileType();
    private SvgaFileType() {
        super(SvgaLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "svga";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "svga";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "svga";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return SvgaIcon.FILE;
    }
}