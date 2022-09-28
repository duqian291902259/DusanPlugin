package site.duqian.plugin.svga;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.impl.text.TextEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.duqian.plugin.base.IOUtil;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public final class SvgaFileMainImpl extends UserDataHolderBase implements FileEditor {

    private static final String NAME = "SVGA-Player";
    private final VirtualFile mFile;
    private final String mRootPath;

    public SvgaFileMainImpl(@NotNull Project project, @NotNull VirtualFile file) {
        mFile = file;
        mRootPath = project.getBasePath();
        mLastFile = "";
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        String htmlContent = SvgaDataProcessor.processSvgaData(mFile);
        String text = "rootDir=" + mRootPath + "\nhtmlContent=" + mFile.getPath() + "\n,content=" + htmlContent;
        JPanel jPanel = new JPanel();
        //JTextArea textArea = new JTextArea();
        JLabel label = new JLabel();
        label.setText(text);
        showHtml(htmlContent, label);
        JButton button = new JButton();
        button.setText("点击打开");
        button.addActionListener(e -> {
            mLastFile = "";
            showHtml(htmlContent, label);
        });
        jPanel.add(label);
        jPanel.add(button);
        return jPanel;

        /*if (!JBCefApp.isSupported()) {
            return new JLabel("Not support JBCefApp");
        }
        try {

            JBCefBrowser browser = new JBCefBrowser("http://www.duqian.site/");
            browser.loadHTML(htmlContent);
            jPanel.add(browser.getComponent());
            return jPanel;
        } catch (Exception e) {
            showHtml(htmlContent, textArea);
            return textArea;
        }*/
    }

    private void showHtml(String htmlContent, JLabel label) {
        saveHtmlAndOpenByBrowser(htmlContent);
        String fileSizeText = mFile.getName() + ",size=" + IOUtil.processFileSizeText(mFile.getPath());
        label.setText("当前IDE暂时不支持实时预览动画，将使用系统默认浏览器展示动画效果！\n" + fileSizeText);
        //label.setVisible(false);
    }

    @Override
    public @Nullable VirtualFile getFile() {
        return mFile;
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
            String fileName = "svga.html";
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
