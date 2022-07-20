package site.duqian.plugin.note.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import site.duqian.plugin.note.dialog.AddNoteDialog;

/**
 * 添加md note action
 */
public class AddMdNoteAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取鼠标选中的文本
        String selectedText = e.getRequiredData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        // 获取当前右键的文件名
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        String fileName =  virtualFile.getName();
        // 显示弹框，填写标题和描述
        AddNoteDialog addNoteDialog = new AddNoteDialog(selectedText,fileName);
        addNoteDialog.showAndGet();
    }
}
