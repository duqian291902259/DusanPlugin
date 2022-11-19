package site.duqian.plugin.note.data;

/**
 * md note
 */
public class MdNote implements java.io.Serializable{
    private String title;
    private String remark;
    private String content;
    private String fileName;
    private String fileType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public String toString() {
        return "MdPick{" +
                "title='" + title + '\'' +
                ", remark='" + remark + '\'' +
                ", content='" + content + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                '}';
    }

    public Object[] toRow() {
        return new Object[]{title,remark,fileName,content};
    }
}
