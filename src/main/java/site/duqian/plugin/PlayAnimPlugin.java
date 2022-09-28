package site.duqian.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import site.duqian.plugin.lottie.LottieJsonProcessor;
import site.duqian.plugin.base.IOUtil;
import site.duqian.plugin.svga.SvgaDataProcessor;

import java.awt.*;
import java.io.File;

/**
 * 预览SVGA动画
 */
public class PlayAnimPlugin extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        String basePath = project != null ? project.getBasePath() : "";

        // 获取当前右键的文件名
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        String fileName = virtualFile != null ? virtualFile.getName() : "";

        boolean isSvgaFile = fileName.endsWith(".svga");
        boolean isJsonFile = fileName.endsWith(".json");

        String directory = basePath + File.separator + "build" + File.separator;

        if (isJsonFile) {
            String jsonFileContent = LottieJsonProcessor.get().processData(virtualFile);
            saveHtmlAndOpenByBrowser(directory, "lottie.html", jsonFileContent);
        } else if (isSvgaFile) {
            String htmlContent = SvgaDataProcessor.processSvgaData(virtualFile);
            //String text = "basePath=" + basePath + "\nhtmlContent=" + virtualFile.getPath() + "\n,content=" + htmlContent;
            //System.out.println(text);
            saveHtmlAndOpenByBrowser(directory, "svga.html", htmlContent);
        } else {
            Messages.showMessageDialog("目前只支持预览SVGA/Lottie动画文件，无法预览 " + fileName, "错误提示", Messages.getInformationIcon());
        }
    }

    private void saveHtmlAndOpenByBrowser(String directory, String fileName, String htmlContent) {
        try {
            File file = new File(directory);
            if (file.exists()) {
                file.delete();
            } else {
                file.mkdirs();
            }
            //String fileName = "index.html";
            IOUtil.saveFile(directory, fileName, htmlContent);

            // browse("/Users/duqian/Movies/svga/index.html");
            browse(directory + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void browse(String filePath) {
        if (filePath == null || "".equals(filePath)) return;
        //是否支持桌面
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                // 使用默认浏览器打开链接
                //desktop.browse(new URI("http://www.duqian.site/"));
                //打开本地的文件
                desktop.open(new File(filePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Messages.showMessageDialog("当前平台不支持Desktop功能", "错误提示", Messages.getErrorIcon());
            System.out.println("当前平台不支持 Desktop");
        }
    }

}
