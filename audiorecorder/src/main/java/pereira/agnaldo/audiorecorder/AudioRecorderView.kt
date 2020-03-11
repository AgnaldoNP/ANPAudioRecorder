package pereira.agnaldo.audiorecorder

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.github.windsekirun.naraeaudiorecorder.NaraeAudioRecorder
import com.github.windsekirun.naraeaudiorecorder.chunk.AudioChunk
import com.github.windsekirun.naraeaudiorecorder.config.AudioRecordConfig
import com.github.windsekirun.naraeaudiorecorder.model.RecordState
import com.github.windsekirun.naraeaudiorecorder.source.NoiseAudioSource
import kotlinx.android.synthetic.main.audio_recorder_layout.view.*
import java.io.File
import java.io.FileInputStream


class AudioRecorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


    private var audioCapture = AudioCapture(0)
    private val mediaPlayer = MediaPlayer()
    private var isMediaPlayerPrepared = false

    private lateinit var audioRecorder: NaraeAudioRecorder
    private val recordFile =
        File(context.cacheDir, "record3.wav")
    private var isRecording = false

    init {
        orientation = VERTICAL
        getStyles(attrs, defStyleAttr)

        LayoutInflater.from(context).inflate(R.layout.audio_recorder_layout, this)
        play_button.setOnClickListener { onPlayButtonClicked() }
        pause_button.setOnClickListener { onPauseButtonClicked() }
        stop_button.setOnClickListener { onStopButtonClicked() }
        record_button.setOnClickListener { onRecordClicked() }
        delete_button.setOnClickListener { onDeleteRecord() }

        audioCapture.setFftListener(object : AudioCapture.FFTListener {
            override fun onAudioCaptured(bytes: ByteArray?) {
                onFftlistenerReceived(bytes)
            }
        })
    }

    private fun initializeAudioRecorder() {
        val recordConfig = AudioRecordConfig.defaultConfig()
        val audioSource = NoiseAudioSource(recordConfig)

        audioRecorder = NaraeAudioRecorder()
        audioRecorder.create {
            this.destFile = recordFile
            this.recordConfig = recordConfig
            this.audioSource = audioSource
            this.debugMode = true
            timerCountCallback = { currentTime, maxTime ->
                onTimerCountCallbackReceive(currentTime, maxTime)
            }
            chunkAvailableCallback = { onAudioRecorderCallbackReceive(it) }
        }
        audioRecorder.setOnRecordStateChangeListener { recordState ->
            onRecordStateChangeListener(recordState)
        }
    }

    private fun getStyles(attrs: AttributeSet?, defStyle: Int) {
//        attrs?.let {
//            val typedArray = context.obtainStyledAttributes(
//                attrs,
//                R.styleable.ImageCollectionView, defStyle, R.style.defaultPreviewImageCollection
//            )
//            pinchToZoom = typedArray.getBoolean(
//                R.styleable.ImageCollectionView_pinchToZoom, pinchToZoom
//            )
//            typedArray.recycle()
//        }
    }

    ////////////////////////////////record/////////////////////////////
    private fun onRecordClicked() {
        play_button.isVisible = false
        pause_button.isVisible = false
        record_button.isVisible = false
        stop_button.isVisible = true
        delete_button.isVisible = false
        horizontal_wave.isVisible = true

        if (!isRecording) {
            initializeAudioRecorder()
            audioRecorder.startRecording(context)
        }
    }

    private fun onStopButtonClicked() {
        play_button.isVisible = true
        pause_button.isVisible = false
        record_button.isVisible = false
        stop_button.isVisible = false
        delete_button.isVisible = true
        horizontal_wave.isVisible = false

        if (isRecording) {
            audioRecorder.stopRecording()
        }
    }

    private fun onAudioRecorderCallbackReceive(audioChunk: AudioChunk) {
        val byteArray = audioChunk.toByteArray()
        horizontal_wave.updateAudioWave(byteArray)
    }

    private fun onTimerCountCallbackReceive(currentTime: Long, maxTime: Long) {
        android.util.Log.d("ANP", "currentTime: " + currentTime);
    }

    private fun onRecordStateChangeListener(recordState: RecordState) {
        isRecording = recordState == RecordState.START
    }


    ///////////////////////////// play pause///////////////////////
    private fun onPlayButtonClicked() {
        play_button.isVisible = false
        pause_button.isVisible = true
        record_button.isVisible = false
        stop_button.isVisible = false
        delete_button.isVisible = true
        horizontal_wave.isVisible = false

        audioCapture.start()

        if (!isMediaPlayerPrepared) {
            val fis = FileInputStream(recordFile)
            try {
                mediaPlayer.setDataSource(fis.fd)
                mediaPlayer.isLooping = false
                mediaPlayer.prepare()
                mediaPlayer.setOnPreparedListener { onMediaPlayerPrepared() }
                mediaPlayer.setOnCompletionListener { onMediaPlayerCompleted() }
                mediaPlayer.setOnSeekCompleteListener { onSeekCompleteListener(it) }
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                try {
                    fis.close();
                } catch (ex1: Exception) {
                    ex1.printStackTrace()
                }
            }
        } else {
            startMediaPlayer()
        }

    }

    private fun onMediaPlayerPrepared() {
        isMediaPlayerPrepared = true
        startMediaPlayer()
    }

    private fun onMediaPlayerCompleted() {
        onPauseButtonClicked()
    }

    private fun onSeekCompleteListener(mediaPlayer: MediaPlayer) {

    }

    private fun startMediaPlayer() {
        if (isMediaPlayerPrepared) {
            mediaPlayer.start()
            audioCapture.start()
        }
    }

    private fun onPauseButtonClicked() {
        play_button.isVisible = true
        pause_button.isVisible = false
        record_button.isVisible = false
        stop_button.isVisible = false
        delete_button.isVisible = true
        horizontal_wave.isVisible = false

        audioCapture.stop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    private fun onFftlistenerReceived(bytes: ByteArray?) {
        val amp = Helper.map(Math.abs((bytes?.get(0) ?: 0).toFloat()), 0F, 256F, 10F, 50000F)
//                audioAmplitudeView?.update(amp.toInt());
    }


    ///////////////////////////////////////delete ////////////////////////////
    private fun onDeleteRecord() {
        play_button.isVisible = false
        pause_button.isVisible = false
        record_button.isVisible = true
        stop_button.isVisible = false
        delete_button.isVisible = false
        horizontal_wave.isVisible = true

        horizontal_wave.clearWave()

        if (isMediaPlayerPrepared) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            isMediaPlayerPrepared = false
        }
    }

}