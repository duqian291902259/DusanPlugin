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
    public void update(AnActionEvent event) {
        //在Action显示之前,根据选中文件扩展名判定是否显示此Action
        boolean isShowMenu = isShowMenu(event);
        event.getPresentation().setEnabled(isShowMenu);
        event.getPresentation().setVisible(isShowMenu);
    }

    private static boolean isShowMenu(AnActionEvent event) {
        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) return false;
        String fileName = virtualFile.getName();
        boolean isSvgaFile = fileName.endsWith(".svga");
        boolean isJsonFile = fileName.endsWith(".json");
        //String extension = virtualFile == null ? null : virtualFile.getExtension();
        //"svga".equalsIgnoreCase(extension) || "json".equalsIgnoreCase(extension);
        return isSvgaFile || isJsonFile;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        String basePath = project != null ? project.getBasePath() : "";

        // 获取当前右键的文件名
        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) return;
        String fileName = virtualFile.getName().toLowerCase();
        //String extension = virtualFile.getExtension();
        boolean isSvgaFile = fileName.endsWith(".svga");//"svga".equalsIgnoreCase(extension); //
        boolean isJsonFile = fileName.endsWith(".json");//"json".equalsIgnoreCase(extension);

        String directory = basePath + File.separator + "build" + File.separator;

        if (isJsonFile) {
            String jsonFileContent = LottieJsonProcessor.get().processData(virtualFile);
            saveHtmlAndOpenByBrowser(directory, "lottie.html", jsonFileContent);
        } else if (isSvgaFile) {
            String htmlContent = SvgaDataProcessor.processSvgaData(virtualFile);
            saveHtmlAndOpenByBrowser(directory, "svga.html", htmlContent);
        } else {
            Messages.showMessageDialog("目前只支持预览SVGA/Lottie动画文件，无法预览 " + fileName, "错误提示", Messages.getInformationIcon());
        }
    }

    private void saveHtmlAndOpenByBrowser(String directory, String fileName, String htmlContent) {
        try {
            IOUtil.saveFile(directory, fileName, htmlContent);
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
