package com.gregoiredelannoy.b2filebrowser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import okhttp3.ResponseBody
import java.io.*
import android.util.Base64
import android.util.Log


class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ListAdapter
    internal val b2Files = mutableListOf<FileDescription>()
    private var currentlyDisplayedNode: Node = Node(parent = null, children = null, name = "", path = "", fileDescription = null)
    internal var downloadUrl = ""
    internal var authorizationToken = ""
    internal lateinit var logger: Logger

    internal fun createVirtualFileSystem(list: List<FileDescription>){
        logger.log(Log.DEBUG, "creating vFS with ${list.size} items")
        val fs = VirtualFileSystem(list)
        if(fs.root.children == null){
            throw Exception("Expecting fs.root.children to be a mutableList, as defined in fs class")
        } else {
            updateView(fs.root.children.toList())
        }
    }

    private fun updateOnClick(node: Node){
        toolbarTextView.text = node.path        // Update top level title in app to folder's name

        currentlyDisplayedNode = node
        if (node.children != null){
            updateView(node.children.toList())
        } else {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
            if (sharedPreferences.getBoolean("allowDownload", false)) {
                //TODO("Notify download and state (using https://developer.android.com/training/notify-user/build-notification )")
                logger.log(Log.INFO, "Clicked on a leaf, downloading it: ${node.name}")
                if (node.fileDescription?.fileId != null){
                    b2Download(node.fileDescription.fileId, node.name)
                }
            }
        }
    }

    private fun updateView(list: List<Node>){
        adapter = ListAdapter(list, ::updateOnClick)
        mainListView.layoutManager = LinearLayoutManager(this)
        mainListView.adapter = adapter
    }

    internal fun b2getFiles(apiUrl: String, authorizationToken: String, bucketId: String?, startFileName: String?){
        val usableBucketId = if (bucketId == null){
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
            sharedPreferences.getString("bucketId", "") ?: throw Exception("Unable to find any bucketId to use")
        } else {
            bucketId
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("$apiUrl/b2api/v2/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()


        val service = retrofit.create(B2ApiService::class.java)

        val bucketIdObject = ListFilesQuery()
        bucketIdObject.bucketId = usableBucketId
        bucketIdObject.startFileName = startFileName

        val call = service.listFileNames(authorizationToken, bucketIdObject)

        call.enqueue(object : Callback<FilesArray> {
            override fun onResponse(call: Call<FilesArray>, response: Response<FilesArray>) {
                logger.log(Log.DEBUG, "In getFiles onResponse callback with response code ${response.code()}")
                println(response.errorBody()?.charStream())
                if (response.code() == 200) {
                    val filesArrayResponse = response.body()
                    if (filesArrayResponse == null){
                        logger.log(Log.ERROR, "Null FilesArrayResponse from b2 api")
                    } else {
                        if (filesArrayResponse.files == null){
                            logger.log(Log.ERROR, "files field null in FilesArrayResponse")
                        } else {
                            b2Files.addAll(filesArrayResponse.files)
                            logger.log(Log.INFO, "Received files Array with ${filesArrayResponse.files.size} items. Total # of files: ${b2Files.size}")
                            if (filesArrayResponse.nextFileName != null) {
                                b2getFiles(apiUrl, authorizationToken, filesArrayResponse.nextFileName, usableBucketId)
                            } else {
                                logger.log(Log.INFO, "All files fetched, build tree")
                                createVirtualFileSystem(b2Files)
                            }
                        }
                    }
                }
            }
            override fun onFailure(call: Call<FilesArray>, t: Throwable) {
                logger.log(Log.ERROR, "In getFiles onFailure callback")
            }
        })
    }

    private fun authorizationTokenFromSettings(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
        val username = sharedPreferences.getString("applicationAuthorizationId", "")
        val password = sharedPreferences.getString("applicationAuthorizationKey", "")


        val credentials = "$username:$password"
        return "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
    }

    private fun b2GetToken(){
        val baseUrl = "https://api.backblazeb2.com/b2api/v2/"

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(AuthorizeAccountService::class.java)
        val token = authorizationTokenFromSettings()

        val call = service.getAuthorizationToken(token)
        call.enqueue(object : Callback<AuthorizeAccount> {
            override fun onResponse(call: Call<AuthorizeAccount>, response: Response<AuthorizeAccount>) {
                logger.log(Log.INFO, "In getToken onResponse callback with response code ${response.code()}")
                if (response.code() == 200) {
                    val authorizeAccountResponse = response.body()
                    if (authorizeAccountResponse == null){
                        logger.log(Log.ERROR, "null authoriseAccountResponse")
                    } else {
                        println(authorizeAccountResponse.toString())
                        println(authorizeAccountResponse.apiUrl)
                        authorizationToken = authorizeAccountResponse.authorizationToken
                        downloadUrl = authorizeAccountResponse.downloadUrl
                        b2getFiles(authorizeAccountResponse.apiUrl, authorizeAccountResponse.authorizationToken, authorizeAccountResponse.allowed.bucketId, null)
                    }
                }

            }
            override fun onFailure(call: Call<AuthorizeAccount>, t: Throwable) {
                logger.log(Log.ERROR, "In getToken onFailure callback")
            }
        })
    }

    private fun b2ToLocalView(){
        //TODO("Chain functions!!! Ugly mess of hardcoded callbacks")
        b2GetToken()
    }

    private fun b2Download(fileId: String, fileName: String){
        val retrofit = Retrofit.Builder()
            .baseUrl("$downloadUrl/b2api/v2/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        val service = retrofit.create(B2DownloadService::class.java)
        val call = service.download(authorizationToken, fileId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                logger.log(Log.INFO, "In download onResponse callback with response code ${response.code()}")
                if (response.code() == 200) {
                    val body = response.body()
                    if (body != null){
                        writeResponseBodyToDisk(body, fileName)
                    }
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                logger.log(Log.ERROR, "In download onFailure callback")
            }
        })
    }

    internal fun writeResponseBodyToDisk(body: ResponseBody, fileName: String) {
        val defaultFolder = "/storage/emulated/0/Download/"
        try {
            val path = defaultFolder + fileName
            logger.log(Log.INFO,"Gonna write file to $path")
            val file = File(path)

            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0

                inputStream = body.byteStream()
                outputStream = FileOutputStream(file)

                while (true) {
                    val read = inputStream.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream.write(fileReader, 0, read)

                    fileSizeDownloaded += read.toLong()

                    logger.log(Log.VERBOSE, "file download: $fileSizeDownloaded of $fileSize")
                }

                outputStream.flush()

                logger.log(Log.INFO, "Download successful")

            } catch (e: IOException) {
                logger.log(Log.ERROR, "IOException while in the process of writing downloaded file")
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            logger.log(Log.ERROR, "IOException opening downloaded or local streams")
        }
    }

    override fun onBackPressed() {
        val maybeParent = currentlyDisplayedNode.parent
        if( maybeParent != null){
            updateOnClick(maybeParent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logger = Logger(statusTextView)

        settingsButton.setOnClickListener{
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        b2ToLocalView()
    }
}
