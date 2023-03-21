package site.duqian.plugin.base

import java.io.FileInputStream
import java.util.*


/**
 * Description:
 *
 * Created by 杜乾 on 2023/3/10 - 11:00.
 * E-mail: duqian2010@gmail.com
 */
object CommonUtil {
    fun loadProperties(path: String): Properties? {
        try {
            val properties = Properties()
            properties.load(FileInputStream(path))
            println(properties.toString())
            return properties
        } catch (e: Exception) {
            println("loadProperties error $e")
        }
        return null
    }
}