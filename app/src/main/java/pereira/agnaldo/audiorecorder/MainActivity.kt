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

        recordView.setOnFinishRecord { file ->
            // Do something with file
        }

        recordView.setOnFinishRecord (object : AudioRecorderView.OnFinishRecordListener{
            override fun onFinishRecordListener(file: File) {
                // Do something with file
            }
        })
    }
}
