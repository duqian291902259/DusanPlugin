package site.duqian.plugin.note.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;
import site.duqian.plugin.note.data.DataBus;
import site.duqian.plugin.note.file.IFile;
import site.duqian.plugin.note.file.impl.FreemarkerFileImpl;
import javax.swing.*;

/**
 * md note tool window ui
 */
public class MdNoteUI {
    private JButton mdBtn;
    private JButton resetBtn;
    private JTable dataTable;
    private JTextField titleField;
    private JPanel rootPanel;
    private JLabel titleLabel;

    private Project project;

    public MdNoteUI(Project project) {
        this.project = project;
        // 设置表model
        dataTable.setModel(DataBus.TABLE_MODEL);
        // 保存到md的action
        mdBtn.addActionListener(e -> {
            String text = titleField.getText();
            String fileName = text + ".md";
            if (StringUtils.isEmpty(text)) {
                MessageDialogBuilder.yesNo("Tips", "filename can't be null").show();
            } else {
                VirtualFile virtualFile = FileChooser.chooseFile(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project, project.getBaseDir());
                if (virtualFile != null) {
                    String filePath = virtualFile.getPath() + "/" + fileName;
                    System.out.printf("save to :" + filePath);
                    // 写文件接口
                    IFile template = new FreemarkerFileImpl(DataBus.DATA_LIST, filePath);
                    template.render();
                    MessageDialogBuilder.yesNo("Tips", "Congratulations on successfully generating the md file").show();
                }
            }
        });
        resetBtn.addActionListener(e -> {
            DataBus.reset();
            titleField.setText("");
        });
    }

    public JComponent view() {
        return rootPanel;
    }
}
