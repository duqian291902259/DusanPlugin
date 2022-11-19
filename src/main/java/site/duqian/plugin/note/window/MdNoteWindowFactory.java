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
 * md note window
 */
public class MdNoteWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        ContentFactory contentFactory = contentManager.getFactory();
        MdNoteUI mdNoteUI = new MdNoteUI(project);
        Content content = contentFactory.createContent(mdNoteUI.view(),"main",true);
        contentManager.addContent(content);
    }
}
