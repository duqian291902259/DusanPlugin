package site.duqian.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class HelloPlugin extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        String basePath = project != null ? project.getBasePath() : "";
        Messages.showMessageDialog("Hello，Flat! " + basePath,
                "杜小菜",
                Messages.getInformationIcon());
    }
}
