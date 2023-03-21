package site.duqian.plugin.downloader

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Description:简易版本的下载器，没有做太多封装
 *
 * Created by 杜乾 on 2022/12/23 - 11:11.
 * E-mail:duqian2010@gmail.com
 */
object DownloadManager {
    private const val TAG = "dq-DownloadUtil"
    private var mDownloadingUrl: String? = ""
    private const val sBufferSize = 1024 * 8
    private val mDownloadMap: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    @Synchronized
    fun download(url: String, path: String, downloadListener: DownloadListener) {
        if (mDownloadingUrl == url || url == "") {
            println("dq-download ignore download")
            return
        }
        try {
            val file = File(path)
            file.parentFile?.mkdirs()
            val downloadedBytes = file.length()
            mDownloadingUrl = url
            updateDownloadStatus(url, DownloadStatus.STATUS_WAITING)
            val threadPool = ThreadManager.downloadPool.mPool //Executors.newFixedThreadPool(5)
            val retrofit: Retrofit =
                Retrofit.Builder().baseUrl("https://www.google.com/").callbackExecutor(threadPool).build()
            val service = retrofit.create(
                DownloadService::class.java
            )
            var call: Call<ResponseBody>? = service.download(url)
            if (downloadedBytes > 0) {
                val range = "bytes=$downloadedBytes-"
                call = service.download(url, range)
            }
            call?.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    //将Response写入到从磁盘中，运行在子线程中的
                    writeResponseToDisk(url, path, response, downloadListener)
                }

                override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                    onDownloadFailed(url, downloadListener, throwable)
                }
            })
        } catch (e: Exception) {
            onDownloadFailed(url, downloadListener, e)
        }
    }

    private fun updateDownloadStatus(url: String, status: String) {
        mDownloadMap[url] = status
    }

    fun pauseAllDownload() {
        for ((key, value) in mDownloadMap) {
            mDownloadMap[key] = DownloadStatus.STATUS_PAUSED
            println("$TAG old value =$value,mDownloadMap[key]=${mDownloadMap[key]}")
        }
    }

    private fun isDownloading(url: String): Boolean {
        val isDownloading = mDownloadMap[url] == DownloadStatus.STATUS_DOWNLOADING
        if (!isDownloading) {
            println("$TAG $url,isDownloading=$isDownloading")
        }
        return isDownloading
    }

    private fun onDownloadFailed(
        url: String, downloadListener: DownloadListener?, throwable: Throwable
    ) {
        downloadListener?.onDownloadFailed(throwable)
        mDownloadingUrl = ""
        updateDownloadStatus(url, DownloadStatus.STATUS_ERROR)
    }

    private fun writeResponseToDisk(
        url: String, path: String, response: Response<ResponseBody>, downloadListener: DownloadListener
    ) {
        //从response获取输入流以及总大小
        val body = response.body()
        val code = response.code()
        println("$TAG download code=$code")

        if ((code == 200 || code == 206) && body != null) {
            updateDownloadStatus(url, DownloadStatus.STATUS_DOWNLOADING)
            writeFileFromIS(
                code, url, File(path), body.byteStream(), body.contentLength(), downloadListener
            )
        } else {
            onDownloadFailed(url, downloadListener, RuntimeException("no body"))
        }
    }

    //将输入流写入文件
    private fun writeFileFromIS(
        code: Int, url: String, file: File, `is`: InputStream, totalLength: Long, downloadListener: DownloadListener?
    ) {
        //开始下载
        //downloadListener?.onStart()
        var currentLength: Long = 0

        //创建文件
        if (!file.exists()) {
            if (file.parentFile?.exists() == false) file.parentFile?.mkdir()
            try {
                file.createNewFile()
            } catch (e: Exception) {
                onDownloadFailed(url, downloadListener, e)
            }
        } else {
            currentLength = file.length()
        }
        var raf: RandomAccessFile? = null
        if (206 == code) { //断点续传
            currentLength = file.length()
            raf = RandomAccessFile(file, "rw")
            raf.seek(currentLength)
        }
        println("$TAG $code currentLength=$currentLength,totalLength=$totalLength")

        var os: OutputStream? = null
        try {
            os = BufferedOutputStream(FileOutputStream(file))
            val data = ByteArray(sBufferSize)
            var len: Int
            while (`is`.read(data, 0, sBufferSize).also { len = it } != -1 && isDownloading(url)) {
                //os.write(data, 0, len)
                currentLength += len.toLong()
                if (200 == code) {
                    os.write(data, 0, len)
                    os.flush()
                } else if (206 == code) {
                    raf?.write(data, 0, len)
                }
                //计算当前下载进度
                //downloadListener?.onProgress((100 * currentLength / totalLength).toInt())
            }
            println("$TAG downloadVideo isDownloading=${isDownloading(url)},url=$url")
            os.flush()
            //下载完成，并返回保存的文件路径
            onDownloadSuccess(downloadListener, file, url)
        } catch (e: Exception) {
            e.printStackTrace()
            onDownloadFailed(url, downloadListener, e)
        } finally {
            try {
                `is`.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                os?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (raf != null) {
                try {
                    raf.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun onDownloadSuccess(
        downloadListener: DownloadListener?, file: File, url: String
    ) {
        downloadListener?.onDownloadSuccess(file.absolutePath)
        updateDownloadStatus(url, DownloadStatus.STATUS_DOWNLOADED)
        mDownloadingUrl = ""
    }
}