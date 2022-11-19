package site.duqian.plugin.note.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import site.duqian.plugin.note.dialog.AddNoteDialog;

/**
 * add:md note action
 */
public class AddMdNoteAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        String selectedText = e.getRequiredData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        //Project project = e.getData(PlatformDataKeys.PROJECT);
        String fileName =  virtualFile.getName();
        AddNoteDialog addNoteDialog = new AddNoteDialog(selectedText,fileName);
        addNoteDialog.showAndGet();
    }
}
