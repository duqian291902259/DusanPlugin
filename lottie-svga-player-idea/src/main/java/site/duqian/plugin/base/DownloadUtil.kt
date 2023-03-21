package site.duqian.plugin.base

import site.duqian.plugin.downloader.DownloadListener
import site.duqian.plugin.downloader.DownloadManager
import java.io.File

/**
 * Description:文件下载逻辑
 *
 * Created by 杜乾 on 2023/2/21 - 17:10.
 * E-mail: duqian2010@gmail.com
 */
object DownloadUtil {
    fun startDownload(savedPath: String, videoUrl: String) {
        try {
            val savedFile = File(savedPath)
            if (savedFile.exists() && savedFile.length() > 0) {
                return
            }
            val tempPath = "$savedPath.temp"
            val tempFile = File(tempPath)
            DownloadManager.download(videoUrl, tempPath, object : DownloadListener {
                override fun onDownloadSuccess(path: String?) {
                    println("onDownloadSuccess $savedFile")
                    tempFile.renameTo(savedFile)
                }

                override fun onDownloadFailed(e: Throwable) {
                    println("onDownloadFailed $e")
                    tempFile.delete()
                }
            })
        } catch (e: Exception) {
            println("startDownload error $e")
        }
    }
}