package site.duqian.plugin.dqlab;

import com.android.tools.idea.uibuilder.handlers.motion.editor.adapters.Annotations.NotNull;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;

/**
 * Description: IDEA的最下面窗体中展示
 * <p>
 * Created by 杜乾 on 2023/2/21 - 15:36.
 * E-mail: duqian2010@gmail.com
 */
public class MyToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

        toolWindow.setToHideOnEmptyContent(true);

        class MyPanel extends SimpleToolWindowPanel {

            public MyPanel(boolean vertical) {
                super(vertical);

                DefaultActionGroup group = new DefaultActionGroup();
                group.add(new TestAction("Login1"));
                group.add(new TestAction("Login2"));
                group.add(new TestAction("Login3"));

                try {
                    ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("ToolBar", group, false);
                    setToolbar(toolbar.getComponent());
                } catch (Exception e) {

                }
            }
        }

        // 添加一个tab
        toolWindow.getContentManager().addContent(
                ContentFactory.SERVICE.getInstance().createContent(
                        new MyPanel(false), "DQToolTest", false), 0);

    }
}
