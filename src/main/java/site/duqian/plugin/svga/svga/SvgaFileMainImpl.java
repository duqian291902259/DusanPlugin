package site.duqian.plugin.svga.svga;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.impl.text.TextEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.util.ui.JBUI;

import java.io.File;
import java.net.URI;

import org.jetbrains.annotations.NotNull;
import site.duqian.plugin.svga.util.IOUtil;
import site.duqian.plugin.svga.util.SvgaDataProcessor;
import site.duqian.plugin.webview.*;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

final class SvgaFileMainImpl extends UserDataHolderBase implements FileEditor {

    private static final String NAME = "SVGA-Player";
    private final VirtualFile mFile;
    private final String mRootPath;

    SvgaFileMainImpl(@NotNull Project project, @NotNull VirtualFile file) {
        mFile = file;
        mRootPath = project.getBasePath();
        mLastFile = "";
    }

    private JComponent getBrowser() {
        try {
            return new Browser((BrowserView) Class.forName("site.duqian.plugin.webview.JcefBrowser").newInstance());
        } catch (Exception e) {
            Logger.getInstance(BrowserWindowFactory.class).error(e);
        }

        JLabel label = new JLabel("JCEF is not supported in running IDE");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.TOP);
        label.setBorder(JBUI.Borders.emptyTop(10));

        return label;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        String htmlContent = SvgaDataProcessor.processSvgaData(mFile);
        System.out.println("htmlContent=" + htmlContent);
        /*JComponent jComponent = getBrowser();
        Browser browser = (Browser)jComponent;
        browser.load("https://www.baidu.com/");
        return jComponent;*/
        //String userDir = System.getProperty("user.dir");

        String text = "rootDir=" + mRootPath + "\nhtmlContent=" + mFile.getPath() + "\n,content=" + htmlContent;
        JTextArea textArea = new JTextArea();
        textArea.setText(text);

        /*if (!JBCefApp.isSupported()) {
            browse("/Users/duqian/Movies/svga/index.html");
            return new JLabel("Not support JBCefApp");
        }*/

        try {
            JPanel myPanel = new JPanel();
            JBCefBrowser browser = new JBCefBrowser("http://www.duqian.site/");
            browser.loadHTML(htmlContent);
            myPanel.add(browser.getComponent());
            return myPanel;
        } catch (Exception e) {
            saveHtmlAndOpenByBrowser(htmlContent);
            String fileSizeText = mFile.getName()+",size="+SvgaDataProcessor.processFileSizeText(mFile.getPath());
            textArea.setText("阿哦，当前IDE暂时不支持实时预览动画，将使用系统默认浏览器展示动画效果！\n"+fileSizeText);
            //textArea.setVisible(false);
            return textArea;
        }
    }

    private void saveHtmlAndOpenByBrowser(String htmlContent) {
        try {
            String directory = mRootPath + File.separator + "build" + File.separator;
            File file = new File(directory);
            if (file.exists()) {
                file.delete();
            } else {
                file.mkdirs();
            }
            String fileName = "index.html";
            IOUtil.saveFile(directory, fileName, htmlContent);

            // browse("/Users/duqian/Movies/svga/index.html");
            browse(directory + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String mLastFile = "";//防止再次获取焦点都打开浏览器

    private static void browse(String filePath) {
        if (filePath == null || "".equals(filePath) || filePath.equals(mLastFile)) return;
        mLastFile = filePath;
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
            System.out.println("当前平台不支持 Desktop");
        }
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @NotNull
    @Override
    public String getName() {
        return NAME;
    }

    @NotNull
    @Override
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        return new TextEditorState();
    }

    @Override
    public void setState(@NotNull FileEditorState state) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return mFile.isValid();
    }

    @Override
    public void selectNotify() {
    }

    @Override
    public void deselectNotify() {
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public StructureViewBuilder getStructureViewBuilder() {
        return null;
    }

    @Override
    public void dispose() {
    }
}
