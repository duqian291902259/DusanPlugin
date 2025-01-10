package site.duqian.plugin.base;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.CharsetToolkit;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.Nullable;
import site.duqian.plugin.downloader.ThreadManager;
import sun.nio.cs.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:execute commands
 * <p>
 * Created by 杜乾 on 2023/2/22 - 10:15.
 * E-mail: duqian2010@gmail.com
 */
public class SysCmdUtil {

    private static final String TAG = "SysCmdUtil";

    public static void executeCmd(String cmd, String cmdDir, CmdCallback cmdCallback) {
        List<String> arrayList = new ArrayList<>();
        arrayList.add(cmd);
        executeCmd(arrayList, cmdDir, cmdCallback);
    }

    public static void executeCmd(List<String> arrayList, String cmdDir, CmdCallback cmdCallback) {
        if (arrayList == null || arrayList.size() <= 0) return;
        try {
            GeneralCommandLine commandLine = new GeneralCommandLine(arrayList);
            commandLine.setCharset(StandardCharsets.UTF_8);
            commandLine.setWorkDirectory(cmdDir);

            String commandLineString = commandLine.getCommandLineString();
            LogUtil.INSTANCE.i(TAG + ",cmdString=" + commandLineString);


            CapturingProcessHandler handler = new CapturingProcessHandler(commandLine.createProcess(), new UTF_8(), commandLineString);
            ProcessOutput result = handler.runProcess(10 * 1000);
            boolean isOK = result.getExitCode() == 0;
            String stderr = result.getStderr();
            LogUtil.INSTANCE.i(TAG + ",executeCmd isOK=" + isOK + ",result=" + stderr);
            if (cmdCallback != null) {
                String text = isOK ? stderr : "execute failed:" + result.getExitCode();
                cmdCallback.onResult(isOK, text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void executePython(String cmd, Project project) {
        ThreadManager.INSTANCE.getBackgroundPool().execute(() -> {
            String readLine;
            InputStream in = null;
            InputStreamReader reader = null;
            BufferedReader buffReader = null;
            try {
                // 获取输入流
                String basePath = project != null ? project.getBasePath() : "";
                File dir = new File(basePath);
                in = SysCmdUtil.exec(cmd, dir);
                System.out.println("cmd=" + cmd);
                LogUtil.INSTANCE.i(TAG, "getBasePath=" + project.getBasePath());
                reader = new InputStreamReader(in);
                buffReader = new BufferedReader(reader);
                StringBuffer sb = new StringBuffer();
                while ((readLine = buffReader.readLine()) != null) {
                    LogUtil.INSTANCE.i(TAG + " line=" + readLine);
                    sb.append(readLine + "\n");
                }
                String msg = sb.toString();
                if ("".equals(msg)) {
                    msg = "has not install python3";
                }
                LogUtil.INSTANCE.i(TAG + " msg=" + msg);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtil.close(buffReader);
                IOUtil.close(reader);
                IOUtil.close(in);
            }
        });
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
