package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AppUpdateManager(
    private val repoOwner: String = "nahid6714",
    private val repoName: String = "tools"
) {

    private val apiUrl = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"

    /**
     * Checks for updates from GitHub Releases API.
     * Silent fallback on network/API failure - will never throw or crash the UI.
     */
    suspend fun checkForUpdate(context: Context): UpdateInfo = withContext(Dispatchers.IO) {
        val currentVersionName = BuildConfig.VERSION_NAME
        val currentVersionCode = BuildConfig.VERSION_CODE

        try {
            val url = URL(apiUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                useCaches = false
                defaultUseCaches = false
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "AndroidAppUpdater")
                setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate")
                setRequestProperty("Pragma", "no-cache")
                setRequestProperty("Expires", "0")
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)

                val rawTagName = json.optString("tag_name", "").trim()
                val tagName = rawTagName.removePrefix("v").trim()
                val releaseTitle = json.optString("name", "").trim()
                val rawNotes = json.optString("body", "").trim()
                val cleanNotes = sanitizeReleaseNotes(rawNotes)
                val rawPublishedAt = json.optString("published_at", "")
                val formattedDate = formatReleaseDate(rawPublishedAt)

                // Try to parse remote versionCode from release notes body, release name, or tag name
                var remoteVersionCode = 0
                val codeRegex = Regex("(?i)(?:versionCode|code|build)\\s*[:=]?\\s*(\\d+)")
                val bodyCodeMatch = codeRegex.find(rawNotes)
                if (bodyCodeMatch != null) {
                    remoteVersionCode = bodyCodeMatch.groupValues[1].toIntOrNull() ?: 0
                }
                if (remoteVersionCode == 0) {
                    val titleCodeMatch = codeRegex.find(releaseTitle)
                    if (titleCodeMatch != null) {
                        remoteVersionCode = titleCodeMatch.groupValues[1].toIntOrNull() ?: 0
                    }
                }
                if (remoteVersionCode == 0) {
                    val tagPlusMatch = Regex("\\+(\\d+)").find(rawTagName)
                    if (tagPlusMatch != null) {
                        remoteVersionCode = tagPlusMatch.groupValues[1].toIntOrNull() ?: 0
                    }
                }

                var downloadUrl = ""
                var apkSizeBytes = 0L
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url", "")
                            apkSizeBytes = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                val formattedSize = formatFileSize(apkSizeBytes)
                val isNewer = isVersionNewer(
                    remoteTag = tagName,
                    remoteVersionCode = remoteVersionCode,
                    currentVersionName = currentVersionName,
                    currentVersionCode = currentVersionCode
                )

                if (isNewer) {
                    if (downloadUrl.isNotBlank()) {
                        return@withContext UpdateInfo(
                            hasUpdate = true,
                            latestVersionName = tagName.ifBlank { rawTagName },
                            latestVersionCode = remoteVersionCode,
                            releaseNotes = cleanNotes,
                            downloadUrl = downloadUrl,
                            currentVersionName = currentVersionName,
                            publishedAt = formattedDate,
                            apkSizeFormatted = formattedSize
                        )
                    } else {
                        return@withContext UpdateInfo(
                            hasUpdate = false,
                            currentVersionName = currentVersionName,
                            errorMessage = "নতুন সংস্করণ (${tagName}) সনাক্ত হয়েছে, কিন্তু GitHub-এ কোনো APK ফাইল পাওয়া যায়নি।"
                        )
                    }
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return@withContext UpdateInfo(
                    hasUpdate = false,
                    currentVersionName = currentVersionName,
                    errorMessage = "GitHub রিপ্রোজিটরি অথবা Release পাওয়া যায়নি (HTTP 404)।"
                )
            } else if (responseCode == 403) {
                return@withContext UpdateInfo(
                    hasUpdate = false,
                    currentVersionName = currentVersionName,
                    errorMessage = "GitHub API লিমিট শেষ হয়েছে। কিছুক্ষণ পর আবার চেষ্টা করুন (HTTP 403)।"
                )
            } else {
                return@withContext UpdateInfo(
                    hasUpdate = false,
                    currentVersionName = currentVersionName,
                    errorMessage = "GitHub API রেসপন্স ত্রুটি: HTTP $responseCode"
                )
            }
        } catch (e: java.net.UnknownHostException) {
            return@withContext UpdateInfo(
                hasUpdate = false,
                currentVersionName = currentVersionName,
                errorMessage = "ইন্টারনেট সংযোগ নেই। ইন্টারনেট সংযোগ চেক করে আবার চেষ্টা করুন।"
            )
        } catch (e: java.net.SocketTimeoutException) {
            return@withContext UpdateInfo(
                hasUpdate = false,
                currentVersionName = currentVersionName,
                errorMessage = "GitHub সার্ভার সাড়া দিতে সময় নিচ্ছে (Timeout)। পরে আবার চেষ্টা করুন।"
            )
        } catch (e: Exception) {
            return@withContext UpdateInfo(
                hasUpdate = false,
                currentVersionName = currentVersionName,
                errorMessage = "আপডেট চেক করার সময় সমস্যা হয়েছে: ${e.localizedMessage ?: "অজানা ত্রুটি"}"
            )
        }

        return@withContext UpdateInfo(
            hasUpdate = false,
            currentVersionName = currentVersionName
        )
    }

    private fun formatReleaseDate(rawIsoDate: String): String {
        if (rawIsoDate.isBlank()) return "আজ"
        return try {
            val datePart = rawIsoDate.take(10)
            val parts = datePart.split("-")
            if (parts.size == 3) {
                val year = com.example.util.BengaliUtils.toBengaliDigits(parts[0])
                val month = com.example.util.BengaliUtils.toBengaliDigits(parts[1])
                val day = com.example.util.BengaliUtils.toBengaliDigits(parts[2])
                "$day/$month/$year"
            } else {
                com.example.util.BengaliUtils.toBengaliDigits(datePart)
            }
        } catch (e: Exception) {
            "আজ"
        }
    }

    private fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return ""
        val mb = sizeBytes.toDouble() / (1024 * 1024)
        val formatted = String.format("%.1f MB", mb)
        return com.example.util.BengaliUtils.toBengaliDigits(formatted)
            .replace("MB", "মেগাবাইট")
    }

    /**
     * Compares remote release tag and version code with current version to check if an update is available.
     */
    private fun isVersionNewer(
        remoteTag: String,
        remoteVersionCode: Int,
        currentVersionName: String,
        currentVersionCode: Int
    ): Boolean {
        // 1. If remoteVersionCode was explicitly parsed and is greater than local versionCode
        if (remoteVersionCode > 0 && remoteVersionCode > currentVersionCode) {
            return true
        }
        // 2. If remoteVersionCode was explicitly parsed and is <= local versionCode
        if (remoteVersionCode > 0 && remoteVersionCode <= currentVersionCode) {
            return false
        }

        if (remoteTag.isBlank()) return false

        val remoteClean = remoteTag.removePrefix("v").trim()
        val localClean = currentVersionName.removePrefix("v").trim()

        // Parse versions into numeric components (e.g. "1.0.5" -> [1, 0, 5])
        val remoteParts = remoteClean.split(".").mapNotNull { it.filter { c -> c.isDigit() }.toIntOrNull() }
        val localParts = localClean.split(".").mapNotNull { it.filter { c -> c.isDigit() }.toIntOrNull() }

        if (remoteParts.isNotEmpty() && localParts.isNotEmpty()) {
            val maxLen = maxOf(remoteParts.size, localParts.size)
            for (i in 0 until maxLen) {
                val remoteNum = remoteParts.getOrElse(i) { 0 }
                val localNum = localParts.getOrElse(i) { 0 }
                if (remoteNum > localNum) return true
                if (remoteNum < localNum) return false
            }
            return false
        }

        return remoteClean != localClean && remoteClean.isNotBlank()
    }

    /**
     * Downloads the release APK into the app's cache directory and updates progress state.
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            var urlStr = downloadUrl
            var connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                setRequestProperty("User-Agent", "AndroidAppUpdater")
                instanceFollowRedirects = true
            }

            // Follow HTTP redirects (GitHub Releases redirect to AWS S3)
            var responseCode = connection.responseCode
            var redirectCount = 0
            while ((responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                    responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                    responseCode == HttpURLConnection.HTTP_SEE_OTHER) && redirectCount < 5) {
                val newUrl = connection.getHeaderField("Location")
                if (!newUrl.isNullOrEmpty()) {
                    urlStr = newUrl
                    connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000
                        readTimeout = 30000
                        setRequestProperty("User-Agent", "AndroidAppUpdater")
                    }
                    responseCode = connection.responseCode
                    redirectCount++
                } else break
            }

            val fileLength = connection.contentLength
            val updateDir = File(context.cacheDir, "updates")
            if (!updateDir.exists()) updateDir.mkdirs()

            val apkFile = File(updateDir, "update.apk")
            if (apkFile.exists()) apkFile.delete()

            connection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val data = ByteArray(8192)
                    var total: Long = 0
                    var count: Int
                    while (input.read(data).also { count = it } != -1) {
                        total += count.toLong()
                        if (fileLength > 0) {
                            val progress = ((total * 100) / fileLength).toInt()
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                        output.write(data, 0, count)
                    }
                    output.flush()
                }
            }

            withContext(Dispatchers.Main) {
                onProgress(100)
                onSuccess(apkFile)
                installApk(context, apkFile)
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError(e.localizedMessage ?: "ডাউনলোড করতে ব্যর্থ হয়েছে। ইন্টারনেট কানেকশন চেক করুন।")
            }
        }
    }

    /**
     * Sanitizes raw release notes body from GitHub releases, removing commit hashes,
     * developer commit messages, git technical tags, and formatting into clean Bengali.
     */
    private fun sanitizeReleaseNotes(rawBody: String): String {
        if (rawBody.isBlank()) {
            return "• অ্যাপের পারফরম্যান্স ও অভিজ্ঞতা উন্নত করা হয়েছে।\n• সাধারণ সমস্যা ও বাগ ফিক্স করা হয়েছে।"
        }

        val lines = rawBody.lines()
        val cleanList = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue

            val lower = trimmed.lowercase()
            if (lower.contains("commit:") ||
                lower.contains("message:") ||
                lower.contains("update appupdatemanager") ||
                lower.contains("update build.gradle") ||
                lower.contains("new release v") ||
                trimmed.matches(Regex("(?i).*([0-9a-f]{7,40}).*")) ||
                trimmed.startsWith("#")
            ) {
                continue
            }

            val cleanLine = trimmed
                .replace("**", "")
                .replace("*", "")
                .replace("`", "")
                .trim()

            if (cleanLine.isNotBlank()) {
                if (!cleanLine.startsWith("•") && !cleanLine.startsWith("-")) {
                    cleanList.add("• $cleanLine")
                } else {
                    cleanList.add(cleanLine)
                }
            }
        }

        if (cleanList.isEmpty()) {
            return "• অ্যাপের নতুন ফিচার ও বিভিন্ন উন্নতি অন্তর্ভুক্ত করা হয়েছে।\n• পারফরম্যান্স ও স্থায়িত্ব বৃদ্ধি করা হয়েছে।"
        }

        return cleanList.joinToString("\n")
    }

    /**
     * Launches the Android package installer for the downloaded APK using FileProvider.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            if (!apkFile.exists()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    return
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
