package site.duqian.plugin.svga.svga;

import chrriis.dj.nativeswing.NSComponentOptions;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
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
import org.jetbrains.annotations.NotNull;
import site.duqian.plugin.svga.util.SvgaDataProcessor;

import javax.swing.*;
import java.beans.PropertyChangeListener;
final class SvgaFileEditorImpl extends UserDataHolderBase implements FileEditor {

    private static final String NAME = "SVGA File Editor";
    private final VirtualFile mFile;

    SvgaFileEditorImpl(@NotNull Project project, @NotNull VirtualFile file) {
        mFile = file;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        NativeInterface.open();
        JWebBrowser browser = new JWebBrowser(NSComponentOptions.destroyOnFinalization(),
                NSComponentOptions.constrainVisibility());
        browser.setMenuBarVisible(false);
        browser.setBarsVisible(false);
        browser.setLocationBarVisible(false);
        browser.setButtonBarVisible(false);
        browser.setStatusBarVisible(false);
        browser.setDefaultPopupMenuRegistered(false);
        browser.setJavascriptEnabled(true);
        browser.setVisible(false);
        browser.addWebBrowserListener(new WebBrowserAdapter() {
            @Override
            public void loadingProgressChanged(WebBrowserEvent e) {
                if (e == null) {
                    return;
                }
                JWebBrowser browser = e.getWebBrowser();
                if (browser == null) {
                    return;
                }
                if (browser.getLoadingProgress() == 100) {
                    if (!browser.isVisible()) {
                        browser.setVisible(true);
                    }
                } else {
                    if (browser.isVisible()) {
                        browser.setVisible(false);
                    }
                }
            }
        });
        browser.setHTMLContent(SvgaDataProcessor.processSvgaData(mFile));
        return browser;
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
