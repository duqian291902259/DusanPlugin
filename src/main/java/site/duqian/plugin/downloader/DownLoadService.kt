package site.duqian.plugin.downloader

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url

interface DownloadService {

    @Streaming
    @GET
    fun download(@Url url: String): Call<ResponseBody>?

    @Streaming
    @GET
    fun download(@Url url: String, @Header("Range") range: String): Call<ResponseBody>?
}