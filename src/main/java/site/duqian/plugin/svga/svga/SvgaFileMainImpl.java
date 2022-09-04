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
import com.intellij.util.ui.JBUI;
import groovy.util.logging.Log4j;
import org.jetbrains.annotations.NotNull;
import site.duqian.plugin.svga.util.SvgaDataProcessor;
import site.duqian.plugin.webview.*;

import javax.swing.*;
import java.beans.PropertyChangeListener;

final class SvgaFileMainImpl extends UserDataHolderBase implements FileEditor {

    private static final String NAME = "SVGA File Editor";
    private final VirtualFile mFile;

    SvgaFileMainImpl(@NotNull Project project, @NotNull VirtualFile file) {
        mFile = file;
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
        JLabel label = new JLabel();
        label.setText("htmlContent=" + mFile.getPath() + ",content=" + htmlContent);
        return label;
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
