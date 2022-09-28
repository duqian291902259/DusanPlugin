package site.duqian.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
/**
 * Description:Hi from 杜小菜
 *
 * Created by 杜乾 on 2022/9/28 - 09:39.
 * E-mail: duqian2010@gmail.com
 */
public class HelloPlugin extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        String basePath = project != null ? project.getBasePath() : "";
        Messages.showMessageDialog("Hello，欢迎使用Lottie/SVGA/AndroidPlugins!业余开发各种实用而有趣的AS插件。",
                "Hi from 杜小菜",
                Messages.getInformationIcon());
    }
}
