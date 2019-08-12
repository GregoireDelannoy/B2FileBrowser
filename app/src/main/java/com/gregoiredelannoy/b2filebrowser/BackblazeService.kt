package com.gregoiredelannoy.b2filebrowser

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface AuthorizeAccountService {
    @GET("b2_authorize_account")
    fun getAuthorizationToken(@Header("Authorization") basicAuth: String): Call<AuthorizeAccount>
}

interface B2ApiService {
    @POST("b2_list_file_names")
    fun listFileNames(@Header("Authorization") authorizationToken: String, @Body bucketId: ListFilesQuery): Call<FilesArray>
}

interface B2DownloadService {
    @GET("b2_download_file_by_id")
    fun download(@Header("Authorization") authorizationToken: String, @Query("fileId") fileId: String): Call<ResponseBody>
}