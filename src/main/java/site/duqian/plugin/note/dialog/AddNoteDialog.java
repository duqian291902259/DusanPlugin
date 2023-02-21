package site.duqian.plugin.note.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import site.duqian.plugin.note.data.DataBus;
import site.duqian.plugin.note.data.MdNote;

import javax.swing.*;
import java.awt.*;

/**
 * 添加note的窗口
 */
public class AddNoteDialog extends DialogWrapper {

    private JTextField titleField;
    private JTextArea remarkTextArea;
    private String selectText;
    private String fileName;

    public AddNoteDialog(String selectText,String fileName) {
        super(true);
        this.selectText = selectText;
        this.fileName = fileName;
        setTitle("Add note");
        setModal(true);
        setSize(400,250);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel jPanel = new JPanel(new BorderLayout());
        titleField = new JTextField("Title");
        jPanel.add(titleField,BorderLayout.NORTH);
        remarkTextArea = new JTextArea("Description");
        remarkTextArea.setPreferredSize(new Dimension(300,100));
        jPanel.add(remarkTextArea,BorderLayout.CENTER);
        return jPanel;
    }

    @Override
    protected void doOKAction() {
        String title = titleField.getText();
        String remark = remarkTextArea.getText();
        System.out.println("title="+title+",remark="+remark);
        System.out.println("content="+selectText);

        MdNote mdNote = new MdNote();
        mdNote.setTitle(title);
        mdNote.setRemark(remark);
        mdNote.setContent(selectText);
        mdNote.setFileName(fileName);
        mdNote.setFileType(fileName.substring(fileName.lastIndexOf(".")+1));
        DataBus.addRow(mdNote);
        this.close(OK_EXIT_CODE);
    }
}
