package site.duqian.plugin.downloader


/**
 * Description:下载监听
 *
 * Created by 杜乾 on 2023/2/21 - 17:54.
 * E-mail: duqian2010@gmail.com
 */
interface DownloadListener {
    //fun onStart() {} //下载开始
    //fun onProgress(progress: Int) {} //下载进度
    fun onDownloadSuccess(path: String?) //下载完成
    fun onDownloadFailed(e: Throwable) //下载失败
}