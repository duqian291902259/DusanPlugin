package site.duqian.plugin.base;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * Description:Base 动画文件解析
 * <p>
 * Created by 杜小菜 on 2022/9/28 - 08:09.
 * E-mail: duqian2010@gmail.com
 */
public abstract class IDataProcessor {

    @NotNull
    public String processData(VirtualFile file) {
        if (file == null || !file.exists()) {
            return "";
        }
        String templateFilePath = getTemplateFilePath();
        String htmlContent = processHtml(templateFilePath, file.getPath());
        if (htmlContent == null) {
            return "";
        }
        return htmlContent;
    }

    /**
     * Description:子类重写模版文件地址
     */
    protected abstract String getTemplateFilePath();

    public String processHtml(@NotNull String templatePath, @NotNull String filePath) {
        String templateContent = IOUtil.getFileContent(templatePath);
        if (templateContent == null) {
            return null;
        }

        String fileContent = IOUtil.fileToString(filePath);
        if (fileContent == null || fileContent.equals("")) {
            return null;
        }
        templateContent = replaceTemplateContent(templateContent, fileContent);
        return templateContent;
    }

    protected abstract String replaceTemplateContent(String templateContent, String fileContent);

}
