package pereira.agnaldo.audiorecorder

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                123
            )
        }

        recordView.setOnStartRecording {
            // Do something with file
        }

        recordView.setOnStartRecording(object : AudioRecorderView.OnStartRecordingListener {
            override fun onStartRecording() {
                // Do something with file
            }
        })

        recordView.setOnFinishRecord { file ->
            // Do something with file
        }

        recordView.setOnFinishRecord(object : AudioRecorderView.OnFinishRecordListener {
            override fun onFinishRecordListener(file: File) {
                // Do something with file
            }
        })

        recordView.setOnPlay {
            // Do something with file
        }

        recordView.setOnPlay(object : AudioRecorderView.OnPlayListener {
            override fun onPlay() {
                // Do something with file
            }
        })

        recordView.setOnPause {
            // Do something with file
        }

        recordView.setOnPause(object : AudioRecorderView.OnPauseListener {
            override fun onPause() {
                // Do something with file
            }
        })

        recordView.setOnResume {
            // Do something with file
        }

        recordView.setOnResume(object : AudioRecorderView.OnResumeListener {
            override fun onResume() {
                // Do something with file
            }
        })

        recordView.setOnFinishPlay {
            // Do something with file
        }

        recordView.setOnFinishPlay(object : AudioRecorderView.OnFinishPlayListener {
            override fun onFinishPlayListener() {
                // Do something with file
            }
        })

        recordView.setOnDelete {
            // Do something with file
        }

        recordView.setOnDelete(object : AudioRecorderView.OnDeleteListener {
            override fun onDelete() {
                // Do something with file
            }
        })

    }
}
