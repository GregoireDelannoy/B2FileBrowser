package com.gregoiredelannoy.b2filebrowser

class Allowed {
    var bucketId: String? = null
}

class AuthorizeAccount {
    /* Properties that might be useful later
    var recommendedPartSize: Int = 0 */
    var apiUrl: String = ""
    var authorizationToken: String = ""
    var downloadUrl: String = ""
    var allowed: Allowed = Allowed()
}


class FilesArray {
    val files: List<FileDescription>? = null
    val nextFileName: String? = null
}

class FileDescription {
    /* Properties that might be useful later:
    val accountId: String? = null
    val action: String? = null
    val bucketId: String? = null
    val contentSha1: String? = null
    val contentType: String? = null
    val uploadTimestamp: Long? = null */
    val fileId: String? = null
    val fileName: String? = null
    val contentLength: Long? = null
}