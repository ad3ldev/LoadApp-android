package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        createChannel(CHANNEL_ID, "Download Channel")

        custom_button.setOnClickListener {
            when (radioGroup.checkedRadioButtonId) {
                R.id.choiceGlide -> download(
                    "Glide",
                    getString(R.string.glide_string),
                    glideURL
                )
                R.id.choiceLoadApp -> download(
                    "LoadApp",
                    getString(R.string.load_app_string),
                    udacityURL
                )
                R.id.choiceRetrofit -> download(
                    "Retrofit",
                    getString(R.string.retrofit_string),
                    retrofitURL
                )
                else -> Toast.makeText(
                    applicationContext,
                    "Please select the file to download",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {
                val query = DownloadManager.Query()
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val success =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val downloadTitle =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                    notificationManager.sendNotification(
                        applicationContext,
                        downloadTitle,
                        success
                    )
                }
            }
        }
    }


    private fun download(title: String, description: String, url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(title)
                .setDescription(description)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request) // enqueue puts the download request in the queue.
    }

    companion object {
        private const val udacityURL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val glideURL =
            "https://github.com/bumptech/glide/archive/master.zip"
        private const val retrofitURL =
            "https://github.com/square/retrofit/archive/master.zip"
        private const val CHANNEL_ID = "channelID"
        private const val NOTIFICATION_ID = 0
        private const val REQUEST_CODE = 0
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
                .apply {
                    setShowBadge(false)
                }
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_description)
            notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun NotificationManager.sendNotification(
        applicationContext: Context, title: String, status: Int
    ) {
        val contentIntent = Intent(applicationContext, DetailActivity::class.java)
        pendingIntent = PendingIntent.getActivity(
            applicationContext, NOTIFICATION_ID, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        contentIntent.putExtra("title", title)
        contentIntent.putExtra("status", status)
        val downloadedPendingIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        action = NotificationCompat.Action(
            R.drawable.ic_assistant_black_24dp,
            applicationContext.getString(R.string.notification_title),
            downloadedPendingIntent
        )
        val builder = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(applicationContext.getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                action
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notify(NOTIFICATION_ID, builder.build())
    }
}



