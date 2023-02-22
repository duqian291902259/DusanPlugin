package site.duqian.plugin.svga;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.impl.text.TextEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.duqian.plugin.base.IOUtil;
import site.duqian.plugin.base.Log;
import site.duqian.plugin.downloader.DownloadListener;
import site.duqian.plugin.downloader.DownloadManager;

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

    private static final String tips = "Please input anim resource url ";
    private static final String URL_TEST = "http://res-fq.hiiu.live/hiiu/gift/1657707047035.svga?t=1657707047640";

    private String htmlContent = "";

    @NotNull
    @Override
    public JComponent getComponent() {
        htmlContent = SvgaDataProcessor.processSvgaData(mFile);
        showHtml(htmlContent);
        String text = "rootDir=" + mRootPath + "\nhtmlContent=" + mFile.getPath() + "\n,content=";
        Log.INSTANCE.i(text);

        JPanel jPanel = new JPanel();
        JLabel label1 = new JLabel();

        String fileSizeText = mFile.getName() + ",size=" + IOUtil.processFileSizeText(mFile.getPath());
        label1.setText(fileSizeText);
        JTextArea textArea1 = new JTextArea();
        textArea1.setRows(2);
        textArea1.setBackground(JBColor.BLUE);
        textArea1.requestFocus();
        textArea1.grabFocus();
        textArea1.setAlignmentY(5f);
        textArea1.setFocusable(true);
        textArea1.setSize(300, 50);
        textArea1.setSelectedTextColor(JBColor.white);
        textArea1.setText(URL_TEST);

        //preview button
        JButton button = new JButton();
        button.setText("Preview");
        button.addActionListener(e -> {
            mLastFile = "";
            String inputText = textArea1.getText();
            if (inputText != null) {
                downloadFileAndPreview(inputText, textArea1);
            } else {
                showHtml(htmlContent);
            }
        });

        jPanel.add(label1);
        jPanel.add(textArea1);
        jPanel.add(button);

        return jPanel;
    }

    private void downloadFileAndPreview(String inputText, JTextArea textArea1) {
        mLastFile = "";
        //test
        String resUrl = "";
        if (inputText != null && inputText.startsWith("http")) {
            resUrl = inputText;
        } else {
            //Messages.showMessageDialog("Error", tips, Messages.getErrorIcon());
            textArea1.setText(tips);
            showHtml(htmlContent);
            return;
        }
        String directory = mRootPath + File.separator + "build" + File.separator;
        new File(directory).mkdirs();
        String savedPath = directory + "download.anim";
        System.out.println("savedPath " + savedPath);

        File savedFile = new File(savedPath);
        savedFile.delete();
        String tempPath = "$savedPath.temp";
        File tempFile = new File(tempPath);
        DownloadManager.INSTANCE.download(resUrl, savedPath, new DownloadListener() {
            @Override
            public void onDownloadFailed(@NotNull Throwable e) {
                String failedMsg = "onDownloadFailed " + e;
                System.out.println(failedMsg);
                tempFile.delete();
                SwingUtilities.invokeLater(() -> Messages.showMessageDialog(failedMsg, "Error", Messages.getErrorIcon()));
            }

            @Override
            public void onDownloadSuccess(@Nullable String path) {
                System.out.println("onDownloadSuccess " + path);
                tempFile.renameTo(savedFile);
                String htmlContent = SvgaDataProcessor.processHtml(savedPath);
                showHtml(htmlContent);
            }
        });
    }

    private void showHtml(String htmlContent) {
        saveHtmlAndOpenByBrowser(htmlContent);
        //当前IDE暂时不支持实时预览动画，将使用系统默认浏览器展示动画效果！
        String tips = "Not support preview animation," + "\nwill open browser to show ";
        //label.setText(tips + "\n" + fileSizeText + "\n");
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

    private static String mLastFile = "";//Prevents the preview from opening again

    private static void browse(String filePath) {
        if (filePath == null || "".equals(filePath) || filePath.equals(mLastFile)) return;
        mLastFile = filePath;
        //是否支持桌面
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                //open browser
                //desktop.browse(new URI("http://www.duqian.site/"));
                //open local file
                desktop.open(new File(filePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Current IDE not support Desktop");
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
