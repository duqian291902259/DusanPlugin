package site.duqian.plugin.stopcoding.task;

import site.duqian.plugin.stopcoding.data.DataCenter;
import site.duqian.plugin.stopcoding.ui.TipsDialog;

import java.time.LocalDateTime;
import java.util.TimerTask;

public class WorkTask extends TimerTask {
    @Override
    public void run() {
        DataCenter.status = DataCenter.RESTING;
        DataCenter.nextWorkTime = LocalDateTime.now().plusMinutes(DataCenter.settingData.getRestTime());
        TipsDialog tipsDialog = new TipsDialog();
        tipsDialog.setVisible(true);
    }
}
