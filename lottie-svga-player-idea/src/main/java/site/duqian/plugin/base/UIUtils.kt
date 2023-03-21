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

    fun showMessageDialog(message: String) {
        showMessageDialog("cmd result", message, Messages.getErrorIcon())
    }

    fun checkPython(basePath: String?, isPython3: Boolean) {
        val cmdList: MutableList<String> = ArrayList()
        cmdList.add("which")
        if (isPython3) {
            cmdList.add("python3")
        } else {
            cmdList.add("python")
        }
        SysCmdUtil.executeCmd(cmdList, basePath, object : CmdCallback {
            override fun onResult(success: Boolean, cmdResult: String?) {
                LogUtil.i("check python=$cmdResult,isPython3=$isPython3")
                if (!success) {
                    showMessageDialog("has not install ${cmdList[1]}")
                }
            }
        })
    }
}