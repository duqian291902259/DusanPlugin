package site.duqian.plugin.base

/**
 * Description:cmd命令执行回调
 *
 * Created by 杜乾 on 2023/3/9 - 16:58.
 * E-mail: duqian2010@gmail.com
 */
interface CmdCallback {
    fun onResult(success: Boolean = false, cmdResult: String? = "")
}