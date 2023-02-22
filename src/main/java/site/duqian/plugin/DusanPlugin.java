package site.duqian.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * Description:Hi from 杜小菜
 * <p>
 * Created by 杜小菜 on 2022/9/28 - 09:39.
 * E-mail: duqian2010@gmail.com
 */
public class DusanPlugin extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        //String basePath = project != null ? project.getBasePath() : "";
        Messages.showMessageDialog(project, "Hello，Welcome to Lottie/SVGA/AndroidPlugins!",
                "Hi from Dusan-杜小菜",
                Messages.getInformationIcon());
    }
}
