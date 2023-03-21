package site.duqian.plugin.downloader


/**
 * 文件下载的状态类型
 */
/*@StringDef(
    value = [DownloadStatus.STATUS_WAITING, DownloadStatus.STATUS_DOWNLOADING, DownloadStatus.STATUS_PAUSED, DownloadStatus.STATUS_DOWNLOADED, DownloadStatus.STATUS_ERROR]
)*/
//@Retention(AnnotationRetention.SOURCE)
object DownloadStatus {
    //companion object {
    const val STATUS_WAITING = "waiting"
    const val STATUS_DOWNLOADING = "downloading"
    const val STATUS_PAUSED = "paused"
    const val STATUS_DOWNLOADED = "downloaded"
    const val STATUS_ERROR = "error"
    //}
}
