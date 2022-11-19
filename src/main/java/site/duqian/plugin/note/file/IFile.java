package site.duqian.plugin.note.file;

import java.util.Map;

public interface IFile {

    String templateUri();

    Map<String,Object> data();

    String destPath();

    void render();
}
