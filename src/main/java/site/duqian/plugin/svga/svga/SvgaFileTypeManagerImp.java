package site.duqian.plugin.svga.svga;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class SvgaFileTypeManagerImp extends SvgaFileTypeManager {

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(SvgaFileType.INSTANCE, SvgaFileType.INSTANCE.getDefaultExtension());
    }

    @Override
    public boolean isSvga(VirtualFile file) {
        return file.getFileType() instanceof SvgaFileType;
    }
}
