package site.duqian.plugin.stopcoding.ui;

import site.duqian.plugin.stopcoding.data.DataCenter;
import site.duqian.plugin.stopcoding.task.RestTask;

import javax.swing.*;
import java.awt.event.*;
import java.util.Date;
import java.util.Timer;

public class TipsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel tipsJL;
    private JButton buttonCancel;

    public TipsDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Coding Clock");
        setLocation(400, 200);//screen top left
        setSize(400, 200);//size

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> {
            DataCenter.reskTimer.cancel();
            dispose();
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        DataCenter.reskTimer = new Timer();
        DataCenter.reskTimer.schedule(new RestTask(this), new Date(), 1000);
    }

    private void onOK() {
        handle();
    }

    private void onCancel() {
        handle();
    }

    private void handle() {
        //do nothing!
        JOptionPane.showMessageDialog(null, "When you back,it will auto close.", "Tips", JOptionPane.INFORMATION_MESSAGE);
    }

    public void setDesc(String desc) {
        tipsJL.setText(desc);
    }
}
