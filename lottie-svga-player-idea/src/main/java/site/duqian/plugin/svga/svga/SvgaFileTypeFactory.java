package site.duqian.plugin.svga.svga;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class SvgaFileTypeFactory extends FileTypeFactory {

    static SvgaFileTypeFactory getInstance() {
        return ServiceManager.getService(SvgaFileTypeFactory.class);
    }

    public abstract boolean isSvga(VirtualFile file);
}
