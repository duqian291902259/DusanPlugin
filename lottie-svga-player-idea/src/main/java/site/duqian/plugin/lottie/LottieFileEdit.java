package site.duqian.plugin.lottie;

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
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import site.duqian.plugin.base.IOUtil;
import site.duqian.plugin.base.LogUtil;
import site.duqian.plugin.base.MD5Util;
import site.duqian.plugin.downloader.DownloadListener;
import site.duqian.plugin.downloader.DownloadManager;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Description：handle lottie json file and open
 * 
 * Created by 杜乾 on 2023/3/23 - 14:52.
 * E-mail: duqian2010@gmail.com
 */
public final class LottieFileEdit extends UserDataHolderBase implements FileEditor {

    private static final String NAME = "Lottie-Player";
    private final VirtualFile mFile;
    private final String mRootPath;

    private JPanel mJPanel = null;

    private static final String TIPS = "Please input anim resource url ";

    private String htmlContent = "";

    public LottieFileEdit(@NotNull Project project, @NotNull VirtualFile file) {
        mFile = file;
        mRootPath = project.getBasePath();
        mLastFile = "";
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        if (mJPanel != null) return mJPanel;
        htmlContent = LottieJsonProcessor.get().processData(mFile);

        showHtml(htmlContent);
        String text = "rootDir=" + mRootPath + "\nhtmlContent=" + mFile.getPath() + "\n,content=";
        LogUtil.INSTANCE.i(text);

        JPanel jPanel = new JPanel();
        JLabel label1 = new JLabel();

        try {
            String jsonContent = VfsUtil.loadText(mFile);
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(jsonContent);
            boolean isLottie = jsonObject.has("v") && jsonObject.has("w") && jsonObject.has("h");
            if (!isLottie) {
                label1.setText("not lottie file");
            } else {
                String width = jsonObject.getString("w");
                String height = jsonObject.getString("h");
                String fileSizeText = mFile.getName() + ",size=" + IOUtil.processFileSizeText(mFile.getPath()) + ",width=" + width + ",height=" + height;
                label1.setText(fileSizeText);
            }
        } catch (Exception e) {
            LogUtil.INSTANCE.i("json parse error " + e);
        }

        JTextArea textArea1 = new JTextArea();
        textArea1.setRows(1);
        textArea1.setBackground(JBColor.blue);
        textArea1.requestFocus();
        textArea1.grabFocus();
        textArea1.setFocusable(true);
        textArea1.setSelectedTextColor(JBColor.white);
        textArea1.setText(TIPS);

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
        this.mJPanel = jPanel;
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
            textArea1.setText(TIPS);
            showHtml(htmlContent);
            return;
        }
        String directory = mRootPath + File.separator + "build" + File.separator;
        new File(directory).mkdirs();
        String savedPath = directory + "download." + MD5Util.md5(inputText) + ".json";
        System.out.println("savedPath " + savedPath);

        File savedFile = new File(savedPath);
        if (savedFile.exists() && savedFile.length() > 0) {
            showAnim(savedPath);
            return;
        }
        //savedFile.delete();
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
                if (savedFile.exists() && savedFile.length() > 0) {
                    showAnim(savedPath);
                } else {
                    SwingUtilities.invokeLater(() -> Messages.showMessageDialog("download failed", "Error", Messages.getErrorIcon()));
                }
            }
        });
    }

    private void showAnim(String savedPath) {
        String templateFilePath = LottieJsonProcessor.get().getTemplateFilePath();
        String htmlContent = LottieJsonProcessor.get().processHtml(templateFilePath, savedPath);
        showHtml(htmlContent);
    }

    private void showHtml(String htmlContent) {
        saveHtmlAndOpenByBrowser(htmlContent);
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
            String fileName = "lottie.html";
            IOUtil.saveFile(directory, fileName, htmlContent);
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
