package site.duqian.plugin.base

import com.intellij.openapi.diagnostic.Logger

/**
 * Description:Log util
 *
 * Created by 杜乾 on 2023/2/22 - 10:09.
 * E-mail: duqian2010@gmail.com
 */
object Log {

    private val logger = Logger.getInstance("DQPlugin")

    fun i(msg: String?) {
        logger.info(msg)
    }

    fun i(format: String?, vararg args: Any?) {
        i(String.format(format!!, *args))
    }

    fun w(msg: String?) {
        logger.warn(msg)
    }

    fun w(format: String?, vararg args: Any?) {
        w(String.format(format!!, *args))
    }

    fun w(t: Throwable?) {
        logger.warn(t!!)
    }

    fun w(t: Throwable, format: String?, vararg args: Any?) {
        w(String.format("%s==>%s", String.format(format!!, *args), t.toString()))
    }
}