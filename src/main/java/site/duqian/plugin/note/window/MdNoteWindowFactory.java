package site.duqian.plugin.note.window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import site.duqian.plugin.note.ui.MdNoteUI;

/**
 * md note 视窗提供者
 */
public class MdNoteWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 从toolWindow获取contentManager
        ContentManager contentManager = toolWindow.getContentManager();
        // 从contentManager获取contentFactory
        ContentFactory contentFactory = contentManager.getFactory();
        // contentFactory创建内容
        MdNoteUI mdNoteUI = new MdNoteUI(project);
        Content content = contentFactory.createContent(mdNoteUI.view(),"main",true);
        // 将内容通过contentManager注册到视窗
        contentManager.addContent(content);
    }
}
