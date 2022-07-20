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
        setTitle("add md note");
        setModal(true);
        setSize(400,250);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel jPanel = new JPanel(new BorderLayout());
        titleField = new JTextField("标题");
        jPanel.add(titleField,BorderLayout.NORTH);
        remarkTextArea = new JTextArea("描述");
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
        // 组装数据，加入到数据总线内
        MdNote mdNote = new MdNote();
        mdNote.setTitle(title);
        mdNote.setRemark(remark);
        mdNote.setContent(selectText);
        mdNote.setFileName(fileName);
        mdNote.setFileType(fileName.substring(fileName.lastIndexOf(".")+1));
        // 添加数据
        DataBus.addRow(mdNote);
        this.close(OK_EXIT_CODE);
    }
}
