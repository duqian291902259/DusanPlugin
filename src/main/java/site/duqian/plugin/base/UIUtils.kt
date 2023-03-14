package site.duqian.plugin.base

import com.intellij.openapi.ui.Messages
import javax.swing.Icon
import javax.swing.SwingUtilities

/**
 * Description:Tools for ui tips
 *
 * Created by 杜乾 on 2023/3/9 - 18:38.
 * E-mail: duqian2010@gmail.com
 */
object UIUtils {

    /**
     * show dialog on main thread
     */
    fun showMessageDialog(title: String, message: String, icon: Icon) {
        SwingUtilities.invokeLater {
            Messages.showMessageDialog(
                message, title, icon
            )
        }
    }
}