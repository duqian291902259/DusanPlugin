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
 * Description:Preview SVGA anim
 *
 * Created by 杜乾 on 2022/11/19 - 09:50.
 * E-mail: duqian2010@gmail.com
 */
public class PlayAnimPlugin extends AnAction {
    @Override
    public void update(AnActionEvent event) {
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
        return isSvgaFile || isJsonFile;
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        String basePath = project != null ? project.getBasePath() : "";
        VirtualFile virtualFile = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) return;
        String fileName = virtualFile.getName().toLowerCase();
        boolean isSvgaFile = fileName.endsWith(".svga");
        boolean isJsonFile = fileName.endsWith(".json");

        String directory = basePath + File.separator + "build" + File.separator;

        if (isJsonFile) {
            String jsonFileContent = LottieJsonProcessor.get().processData(virtualFile);
            saveHtmlAndOpenByBrowser(directory, "lottie.html", jsonFileContent);
        } else if (isSvgaFile) {
            String htmlContent = SvgaDataProcessor.processSvgaData(virtualFile);
            saveHtmlAndOpenByBrowser(directory, "svga.html", htmlContent);
        } else {
            Messages.showMessageDialog("Your IDE not support to preview " + fileName, "Error Tips", Messages.getInformationIcon());
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
                //desktop.browse(new URI("http://www.duqian.site/"));
                desktop.open(new File(filePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String tips = "Current Os not support api: Desktop";
            Messages.showMessageDialog(tips, "Error msg", Messages.getErrorIcon());
            System.out.println(tips);
        }
    }

}
