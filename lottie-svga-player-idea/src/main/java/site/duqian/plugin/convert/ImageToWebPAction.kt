package site.duqian.plugin.convert

import com.android.resources.ResourceFolderType
import com.android.tools.idea.rendering.webp.ConvertToWebpAction
import com.android.tools.idea.res.isLocalResourceDirectory
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
        ConvertToWebpAction().actionPerformed(e)
    }

    override fun update(e: AnActionEvent) {
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        if (files != null && e.project != null) {
            for (file in files) {
                val directory = file.isDirectory
                if (directory && isResourceDirectory(file, e.project!!) ||
                    !directory && ConvertToWebpAction.isEligibleForConversion(file, null)
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
            } else isLocalResourceDirectory(file, project)
        }
        return false
    }
}