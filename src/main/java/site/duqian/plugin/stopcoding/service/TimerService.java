package site.duqian.plugin.stopcoding.service;

import site.duqian.plugin.stopcoding.data.DataCenter;
import site.duqian.plugin.stopcoding.data.SettingData;
import site.duqian.plugin.stopcoding.task.WorkTask;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;

public class TimerService {

    public static SettingData saveSetting(boolean selected, String restTimeTFText, String worTimeTFText) {
        DataCenter.settingData = SettingData.of(selected,
                DataCenter.isInteger(restTimeTFText) ? Integer.parseInt(restTimeTFText) : SettingData.DEFAULT_REST_TIME,
                DataCenter.isInteger(worTimeTFText) ? Integer.parseInt(worTimeTFText) : SettingData.DEFAULT_WORK_TIME);
        return DataCenter.settingData;
    }

    public static void restCountDown() {
        DataCenter.restCountDownSecond--;
    }

    public static void initRestCountDown() {
        if (DataCenter.restCountDownSecond == -1) {
            DataCenter.restCountDownSecond = 60 * DataCenter.settingData.getRestTime();
        }
    }

    public static void resetNextWorkTime() {
        DataCenter.nextWorkTime = LocalDateTime.now().plusMinutes(DataCenter.settingData.getRestTime());
    }

    public static void resetNexRestTime() {
        DataCenter.nextRestTime = LocalDateTime.now().plusMinutes(DataCenter.settingData.getWorkTime());
    }

    public static String openTimer() {
        DataCenter.workTimer.cancel();
        DataCenter.workTimer = new Timer();
        resetNexRestTime();
        DataCenter.workTimer.schedule(new WorkTask(), Date.from(DataCenter.nextRestTime.atZone(ZoneId.systemDefault()).toInstant()));
        DataCenter.status = DataCenter.WORKING;
        DataCenter.settingData.setOpen(true);
        return String.format("Start coding for %s minutes, Next rest time：%s", DataCenter.settingData.getWorkTime(), getDateStr(DataCenter.nextRestTime));
    }

    public static String closeTimer() {
        DataCenter.workTimer.cancel();
        DataCenter.settingData.setOpen(false);
        DataCenter.status = DataCenter.CLOSE;
        return "Coding-Clock is off";
    }

    public static String getCountDownDesc(int time) {
        if (time > 0) {
            int hour = time / (60 * 60);
            int minute = (time % (60 * 60)) / 60;
            int second = time % 60;
            return "Have a rest,count down：" + String.format("%s:%s:%s", fillZero(hour), fillZero(minute), fillZero(second));
        }
        return "The rest is over";
    }


    public static String fillZero(int time) {
        if (time < 10) {
            return "0" + time;
        }
        return "" + time;
    }

    public static String getDateStr(LocalDateTime localDateTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm:ss");
        return df.format(localDateTime);
    }

}
