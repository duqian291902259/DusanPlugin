package site.duqian.plugin.svga.svga;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SvgaFileType extends LanguageFileType {

    static final SvgaFileType INSTANCE = new SvgaFileType();

    private SvgaFileType() {
        super(Svga.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "SVGA Animation";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "SVGA Animation";
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