package site.duqian.plugin.svga.svga;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class SvgaFileTypeManager extends FileTypeFactory {

    static SvgaFileTypeManager getInstance() {
        return ServiceManager.getService(SvgaFileTypeManager.class);
    }

    public abstract boolean isSvga(VirtualFile file);
}
