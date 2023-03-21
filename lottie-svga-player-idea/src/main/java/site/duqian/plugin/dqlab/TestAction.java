package site.duqian.plugin.dqlab;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;

/**
 * Description:测试Action
 * <p>
 * Created by 杜乾 on 2023/2/21 - 15:53.
 * E-mail: duqian2010@gmail.com
 */
public class TestAction extends AnAction implements ApplicationComponent {

    private String text;

    public TestAction(String text) {
        this.text = text;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile != null) {
            String classPath = psiFile.getVirtualFile().getPath();
        }
        String msg = "Hello DuQian! " + text;
        Messages.showMessageDialog(project, msg, "Test Title", Messages.getInformationIcon());
    }
}
