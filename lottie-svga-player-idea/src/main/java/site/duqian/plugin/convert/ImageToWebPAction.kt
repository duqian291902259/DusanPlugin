package site.duqian.plugin.convert

import com.android.resources.ResourceFolderType
import com.android.tools.idea.rendering.webp.ConvertToWebpAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Description: Add 'Convert to WebP' in VCS Local Change window
 */
class ImageToWebPAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        //ConvertToWebpAction().actionPerformed(e)
        //ActionManager.getInstance().getAction(IdeActions.ACTION_HIGHLIGHT_USAGES_IN_FILE).actionPerformed(e)

        //fix api问题：ImageToWebPAction.actionPerformed(AnActionEvent). This method is marked with @ApiStatus.OverrideOnly annotation,
        // which indicates that the method must be only overridden but not invoked by client code. See documentation of the @ApiStatus.OverrideOnly for more info.
        // WARNING: WebP requires API 14; current minSdkVersion is 0
        val minSdkVersion = 14
        val folders = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        val action = ConvertToWebpAction()
        action.perform(e.project!!, minSdkVersion, folders)
    }

    override fun update(e: AnActionEvent) {
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        if (files != null && e.project != null) {
            for (file in files) {
                val directory = file.isDirectory
                val fileName = file.name.toLowerCase()
                if (directory && isResourceDirectory(file, e.project!!) || fileName.endsWith(".png")
                    || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")
                ) {
                    e.presentation.isEnabledAndVisible = true
                    return
                }
            }
        }
        e.presentation.isEnabledAndVisible = false
    }

    /**
     * only convert the files in resource folder
     */
    private fun isResourceDirectory(file: VirtualFile, project: Project): Boolean {
        if (file.isDirectory) {
            val folderType = ResourceFolderType.getFolderType(file.name)
            return if (folderType != null) {
                folderType == ResourceFolderType.DRAWABLE || folderType == ResourceFolderType.MIPMAP
            } else false;//isLocalResourceDirectory(file, project)
        }
        return false
    }
}