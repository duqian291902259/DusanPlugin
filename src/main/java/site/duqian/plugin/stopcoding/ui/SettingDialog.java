package site.duqian.plugin.stopcoding.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.ui.Messages;
import site.duqian.plugin.stopcoding.data.DataCenter;
import site.duqian.plugin.stopcoding.data.SettingData;
import site.duqian.plugin.stopcoding.service.TimerService;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SettingDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JRadioButton openRbtn;
    private JTextField workTimeTF;
    private JTextField restTimeTF;
    private JLabel descJL;

    public SettingDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Coding Clock");
        setLocation(400, 200);//top left
        setSize(500, 300);//size

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        openRbtn.addChangeListener(e -> {
            boolean selected = openRbtn.isSelected();
            if (selected) {
                descJL.setText("Coding-Clock is on,Click save to start timer");
            } else {
                descJL.setText("Coding-Clock is off");
            }
            //setCheckBtnText(selected);
        });

        SettingData settings = new SettingData();
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.loadFields(settings);
        DataCenter.settingData = settings;
        openRbtn.setSelected(DataCenter.settingData.isOpen());
        workTimeTF.setText(DataCenter.settingData.getWorkTime() + "");
        restTimeTF.setText(DataCenter.settingData.getRestTime() + "");
        descJL.setText(DataCenter.getSettingDesc());
        //setCheckBtnText(openRbtn.isSelected());
        openRbtn.setText("Select to turn on Coding-Clock");
    }

    private void setCheckBtnText(boolean isSelected) {
        openRbtn.setText(isSelected ? "Turn on Coding-Clock" : "Turn off Coding-Clock");
    }

    private void onOK() {
        SettingData settings = TimerService.saveSetting(openRbtn.isSelected(), restTimeTF.getText(), workTimeTF.getText());
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        propertiesComponent.saveFields(settings);

        String notifyStr;
        if (openRbtn.isSelected()) {
            notifyStr = TimerService.openTimer();
        } else {
            notifyStr = TimerService.closeTimer();
        }
        Messages.showMessageDialog(notifyStr, "Tips", Messages.getInformationIcon());
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        SettingDialog dialog = new SettingDialog();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
