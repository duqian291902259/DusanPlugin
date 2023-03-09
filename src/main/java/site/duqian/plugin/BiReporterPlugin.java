package site.duqian.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import site.duqian.plugin.base.IOUtil;
import site.duqian.plugin.base.LogUtil;
import site.duqian.plugin.base.SysCmdUtil;
import site.duqian.plugin.downloader.ThreadManager;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:execute cmd for bi
 * <p>
 * Created by 杜乾 on 2023/2/22 - 11:26.
 * E-mail: duqian2010@gmail.com
 */
public class BiReporterPlugin extends AnAction {
    public static final String TAG = "dq-plugin";

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        String basePath = project != null ? project.getBasePath() : "";
        System.out.println("basePath=" + basePath);

        String cmd = String.format("which python3");
        LogUtil.INSTANCE.i(TAG + " cmd=" + cmd);

        executePython(cmd, project);
        List<String> cmdList = new ArrayList<>();
        cmdList.add("python3");
        cmdList.add("statistics.py");
        cmdList.add("app-hiiclub-v2.json");
        SysCmdUtil.executeCmd(cmdList, basePath+"/lib_commons");
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
                    msg = "没有安装Python3";
                    String finalMsg = msg;
                    SwingUtilities.invokeLater(() -> Messages.showMessageDialog(finalMsg, "cmd result", Messages.getErrorIcon()));
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
}
