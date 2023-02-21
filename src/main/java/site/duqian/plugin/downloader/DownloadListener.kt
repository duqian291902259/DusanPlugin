package site.duqian.plugin.downloader

interface DownloadListener {
    fun onStart() {} //下载开始
    fun onProgress(progress: Int) {} //下载进度
    fun onDownloadSuccess(path: String?) //下载完成
    fun onDownloadFailed(e: Throwable) //下载失败
}