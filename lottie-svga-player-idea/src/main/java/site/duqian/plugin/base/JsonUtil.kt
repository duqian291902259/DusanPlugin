package site.duqian.plugin.base

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.Exception

/**
 * Description:Json 解析
 *
 * Created by 杜乾 on 2023/3/9 - 19:15.
 * E-mail: duqian2010@gmail.com
 */
object JsonUtil {
    private val gson: Gson by lazy {
        GsonBuilder().serializeNulls().create()
    }

    fun toString(obj: Any): String {
        return gson.toJson(obj)
    }

    fun <T> toBean(json: String, clazz: Class<T>): T? {
        var t: T? = null
        try {
            t = gson.fromJson(json, clazz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return t
    }
}