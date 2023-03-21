package site.duqian.plugin.stopcoding.task;

import site.duqian.plugin.stopcoding.data.DataCenter;
import site.duqian.plugin.stopcoding.service.TimerService;
import site.duqian.plugin.stopcoding.ui.TipsDialog;

import javax.swing.*;
import java.util.TimerTask;

public class RestTask extends TimerTask {
    TipsDialog tipsDialog;

    public RestTask(TipsDialog tipsDialog) {
        this.tipsDialog = tipsDialog;
    }

    @Override
    public void run() {
        TimerService.resetNextWorkTime();
        TimerService.initRestCountDown();
        TimerService.restCountDown();//倒计时
        if (DataCenter.restCountDownSecond >= 0) {
            String desc = TimerService.getCountDownDesc(DataCenter.restCountDownSecond);
            tipsDialog.setDesc(String.format("StopCoding! %s", desc));
        } else {//end
            DataCenter.reskTimer.cancel();   //close
            tipsDialog.dispose(); //关闭提示窗口
            String notifyStr = TimerService.openTimer();// start
            JOptionPane.showMessageDialog(null, notifyStr, "tips", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
