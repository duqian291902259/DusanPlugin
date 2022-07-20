package site.duqian.plugin.note.file;

import java.util.Map;

/**
 * 文件接口
 */
public interface IFile {
    /**
     * template uri设置
     * @return
     */
    String templateUri();

    /**
     * 数据 设置
     * @return
     */
    Map<String,Object> data();

    /**
     * 保存的文件路径
     * @return
     */
    String destPath();

    /**
     * 渲染
     */
    void render();
}
