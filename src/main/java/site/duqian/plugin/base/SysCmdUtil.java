package site.duqian.plugin.base;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import site.duqian.plugin.BiReporterPlugin;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Description:execute commands
 * <p>
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

    public static void executeCmd(List<String> arrayList, String cmdDir) {
        if (arrayList == null || arrayList.size() <= 0) return;
        try {
            GeneralCommandLine commandLine = new GeneralCommandLine(arrayList);
            commandLine.setCharset(StandardCharsets.UTF_8);
            commandLine.setWorkDirectory(cmdDir);

            ProcessHandler processHandler = new OSProcessHandler(commandLine);
            processHandler.startNotify();

            processHandler.addProcessListener(new ProcessAdapter() {
                @Override
                public void startNotified(@NotNull ProcessEvent event) {
                    super.startNotified(event);
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    super.processTerminated(event);
                }

                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    super.onTextAvailable(event, outputType);
                    String text = event.getText();
                    LogUtil.INSTANCE.i(BiReporterPlugin.TAG+ ",dq-cmd=" + text);
                    SwingUtilities.invokeLater(() -> Messages.showMessageDialog(text, "cmd result", Messages.getInformationIcon()));
                }
            });
            //String commandLineOutputStr = ScriptRunnerUtil.getProcessOutput(commandLine);
            //System.out.println("cmd result=" + commandLineOutputStr);
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
                LogUtil.INSTANCE.i("libraryInspect.open.failed");
            }
        }
    }
}
