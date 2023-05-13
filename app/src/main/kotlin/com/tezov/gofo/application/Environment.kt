package com.tezov.gofo.application

import com.tezov.lib_java_android.file.UriW
import com.tezov.gofo.application.SharePreferenceKey.SP_DESTINATION_DIRECTORY_STRING
import com.tezov.lib_java.file.Directory
import com.tezov.lib_java_android.application.AppContext
import com.tezov.lib_java_android.file.StorageTree
import com.tezov.lib_java_android.file.StorageMedia

object Environment {
    //USER
    val DIRECTORY_ROOT = AppContext.getApplicationName() + Directory.PATH_SEPARATOR

    //URI
    fun obtainPendingUri(fileFullName: String): UriW? {
        return obtainUri(fileFullName, object : uriSupplier {
            override fun obtainUriFromUriTree(
                uriTree: StorageTree,
                directory: String?,
                fileFullName: String?
            ): UriW? {
                return uriTree.obtainUri(directory, fileFullName)
            }

            override fun obtainUriFromStorageMedia(
                directory: String?,
                fileFullName: String?
            ): UriW? {
                return StorageMedia.obtainPendingUri(directory, fileFullName)
            }
        })
    }
    fun obtainUniquePendingUri(fileFullName: String): UriW? {
        return obtainUri(fileFullName, object : uriSupplier {
            override fun obtainUriFromUriTree(
                uriTree: StorageTree,
                directory: String?,
                fileFullName: String?
            ): UriW? {
                return uriTree.obtainUniqueUri(directory, fileFullName)
            }

            override fun obtainUriFromStorageMedia(
                directory: String?,
                fileFullName: String?
            ): UriW? {
                return StorageMedia.obtainUniquePendingUri(directory, fileFullName)
            }
        })
    }
    fun obtainClosestPendingUri(fileFullName: String): UriW? {
        return obtainUri(fileFullName, object : uriSupplier {
            override fun obtainUriFromUriTree(
                uriTree: StorageTree,
                directory: String?,
                fileFullName: String?
            ): UriW? {
                return uriTree.obtainClosestUri(directory, fileFullName)
            }

            override fun obtainUriFromStorageMedia(
                directory: String?,
                fileFullName: String?
            ): UriW? {
                return StorageMedia.obtainClosestPendingUri(directory, fileFullName)
            }
        })
    }

    private fun obtainUri(fileFullName: String, supplier: uriSupplier): UriW? {
        var directoryPath = DIRECTORY_ROOT
        var uri: UriW? = null
        val sp = Application.sharedPreferences()
        val destinationFolder = sp.getString(SP_DESTINATION_DIRECTORY_STRING)
        if (destinationFolder != null) {
            val uriTree = StorageTree.fromLink(destinationFolder)
            if (uriTree != null) {
                if (uriTree.canWrite()) {
                    uri = supplier.obtainUriFromUriTree(uriTree, directoryPath, fileFullName)
                } else {
                    sp.remove(SP_DESTINATION_DIRECTORY_STRING)
                }
            }
        }
        if (uri == null) {
            directoryPath =
                StorageMedia.findBestDirectoryForFile(fileFullName) + Directory.PATH_SEPARATOR + directoryPath
            uri = supplier.obtainUriFromStorageMedia(directoryPath, fileFullName)
        }
        return uri
    }
    private interface uriSupplier {
        fun obtainUriFromUriTree(
            uriTree: StorageTree,
            directory: String?,
            fileFullName: String?
        ): UriW?

        fun obtainUriFromStorageMedia(directory: String?, fileFullName: String?): UriW?
    }

}