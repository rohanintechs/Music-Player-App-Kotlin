package com.rohan.musicplayerapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var songTitle: TextView
    private lateinit var leftTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var playBtn: Button
    private lateinit var pauseBtn: Button
    private lateinit var playListBtn: Button
    private val handler = android.os.Handler()

    private val FILE_PICKER_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        songTitle = findViewById(R.id.songTitle)
        leftTime = findViewById(R.id.timeLeft)
        seekBar = findViewById(R.id.seekBar)

        playListBtn = findViewById(R.id.playListBtn)
        playListBtn.setOnClickListener {
            openFilePicker()
        }

        seekBar.isClickable = false
        mediaPlayer = MediaPlayer()

        playBtn = findViewById(R.id.playBtn)
        playBtn.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                playBtn.text = ""
            } else {
                mediaPlayer.start()
                playBtn.text = ""
                val finalTime = mediaPlayer.duration.toDouble()
                val startTime = mediaPlayer.currentPosition.toDouble()
                var oneTimeOnly = 0
                if (oneTimeOnly == 0) {
                    seekBar.max = finalTime.toInt()
                    oneTimeOnly = 1
                }
                leftTime.text = startTime.toString()
                seekBar.progress = startTime.toInt()
                handler.postDelayed(updateSongTime, 100)
            }
        }

        pauseBtn = findViewById(R.id.pauseBtn)
        pauseBtn.setOnClickListener {
            mediaPlayer.pause()
            playBtn.text = ""
        }
    }

    private val updateSongTime: Runnable = object : Runnable {
        override fun run() {
            val startTime = mediaPlayer.currentPosition.toDouble()
            leftTime.text = "" +
                    String.format(
                        "%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes(startTime.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(startTime.toLong())
                                - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(
                                startTime.toLong()
                            )
                        )
                    )
            seekBar.progress = startTime.toInt()
            handler.postDelayed(this, 100)
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedSongUri = data?.data
            if (selectedSongUri != null) {
                mediaPlayer.reset()
                mediaPlayer.setDataSource(this, selectedSongUri)
                mediaPlayer.prepare()
                val songName = getSongName(selectedSongUri)
                songTitle.text = "$songName"
            } else {
                Toast.makeText(this, "Error selecting the song", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSongName(songUri: Uri): String {
        val cursor = contentResolver.query(songUri, null, null, null, null)
        var songName = ""
        cursor?.use {
            it.moveToFirst()
            val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (displayNameIndex != -1) {
                songName = it.getString(displayNameIndex)
            }
        }
        return songName
    }
}
