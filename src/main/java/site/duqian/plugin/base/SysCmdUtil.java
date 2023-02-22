package site.duqian.plugin.base;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;

/**
 * Description:execute commands
 *
 * Created by 杜乾 on 2023/2/22 - 10:15.
 * E-mail: duqian2010@gmail.com
 */
public class SysCmdUtil {

    public static void execUnCareResult(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return;
        }
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * run cmd and care result
     */
    @Nullable
    public static InputStream exec(String cmd) {
        return exec(cmd, null);
    }

    @Nullable
    public static InputStream exec(String cmd, File dir) {
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        Process process;
        try {
            if (dir == null) {
                process = Runtime.getRuntime().exec(cmd);
            } else {
                process = Runtime.getRuntime().exec(cmd, null, dir);
            }
            return process.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Nullable
    public static InputStream exec2(String cmd, File dir) throws Exception {
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        Process process;
        if (dir == null) {
            process = Runtime.getRuntime().exec(cmd);
        } else {
            process = Runtime.getRuntime().exec(cmd, null, dir);
        }
        return process.getInputStream();
    }


    public static void openBrowser(String path) {
        String str = String.format("cmd /c start chrome %s", path);
        try {
            Runtime.getRuntime().exec(str);
        } catch (Exception e) {
            try {
                str = String.format("cmd /c start iexplore %s", path);
                Runtime.getRuntime().exec(str);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void openDirectory(String path) {
        try {
            java.awt.Desktop.getDesktop().open(new File(path));
        } catch (Exception e) {
            try {
                Runtime.getRuntime().exec(String.format("cmd /c explorer.exe /select, %s", path));
            } catch (Exception ex) {
                Log.INSTANCE.i("libraryInspect.open.failed");
            }
        }
    }
}
