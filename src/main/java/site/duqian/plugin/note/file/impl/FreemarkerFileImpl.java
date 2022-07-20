package site.duqian.plugin.note.file.impl;

import site.duqian.plugin.note.data.MdNote;
import site.duqian.plugin.note.file.IFile;
import site.duqian.plugin.note.file.util.FreemarkerUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * freemarker 文件渲染实现
 */
public class FreemarkerFileImpl implements IFile {
    private List<MdNote> mdNotes;
    private String filePath;
    public FreemarkerFileImpl(List<MdNote> mdNotes, String filePath){
        this.mdNotes = mdNotes;
        this.filePath = filePath;
    }

    @Override
    public String templateUri() {
        return "md.ftl";
    }

    @Override
    public Map<String,Object> data() {
        Map<String,Object> data = new HashMap<>();
        data.put("data",mdNotes);
        return data;
    }

    @Override
    public String destPath() {
        return filePath;
    }

    @Override
    public void render() {
        try {
            FreemarkerUtil.genFile(templateUri(),data(),destPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
