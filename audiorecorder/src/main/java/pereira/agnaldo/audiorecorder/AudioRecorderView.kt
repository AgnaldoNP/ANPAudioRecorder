package pereira.agnaldo.audiorecorder

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.isVisible
import com.github.windsekirun.naraeaudiorecorder.NaraeAudioRecorder
import com.github.windsekirun.naraeaudiorecorder.chunk.AudioChunk
import com.github.windsekirun.naraeaudiorecorder.config.AudioRecordConfig
import com.github.windsekirun.naraeaudiorecorder.extensions.runOnUiThread
import com.github.windsekirun.naraeaudiorecorder.model.RecordState
import com.github.windsekirun.naraeaudiorecorder.source.NoiseAudioSource
import kotlinx.android.synthetic.main.audio_recorder_layout.view.*
import java.io.File
import java.io.FileInputStream
import java.util.*


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

    var duration = 0L
    var amoungToUpdate = 0L

    init {
        orientation = VERTICAL
        getStyles(attrs, defStyleAttr)

        LayoutInflater.from(context).inflate(R.layout.audio_recorder_layout, this)
        play_button.setOnClickListener { onPlayButtonClicked() }
        pause_button.setOnClickListener { onPauseButtonClicked() }
        stop_button.setOnClickListener { onStopButtonClicked() }
        record_button.setOnClickListener { onRecordClicked() }
        delete_button.setOnClickListener { onDeleteRecord() }

        play_pause_seek.isVisible = false

        play_pause_seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    setMediaPlayerProgress(progress)
                }
            }
        })

        audioCapture.setFftListener { bytes -> onFftListenerReceived(bytes) }
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
        play_pause_seek.isVisible = false

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
        play_pause_seek.isVisible = true

        if (isRecording) {
            audioRecorder.stopRecording()
        }
    }

    private fun onAudioRecorderCallbackReceive(audioChunk: AudioChunk) {
        val byteArray = audioChunk.toByteArray()
        horizontal_wave.updateAudioWave(byteArray)
    }

    private fun onTimerCountCallbackReceive(currentTime: Long, maxTime: Long) {
        android.util.Log.d("ANP", "currentTime: " + currentTime + "; maxTime: " + maxTime)
        setTimer((currentTime / 1000).toInt())
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
        play_pause_seek.isVisible = true

        audioCapture.start()

        if (!isMediaPlayerPrepared) {
            val fis = FileInputStream(recordFile)
            try {
                mediaPlayer.setDataSource(fis.fd)
                mediaPlayer.isLooping = false
                mediaPlayer.prepare()
                mediaPlayer.setOnPreparedListener { onMediaPlayerPrepared() }
                mediaPlayer.setOnCompletionListener { onMediaPlayerCompleted() }
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
        duration = mediaPlayer.getDuration().toLong()
        amoungToUpdate = duration / 100

        isMediaPlayerPrepared = true
        startMediaPlayer()
    }

    private fun updateMediaPlayerProgress() {
        if (mediaPlayer.isPlaying) {
            play_pause_seek.post {
                if (!(amoungToUpdate * play_pause_seek.progress >= duration)) {
                    val progress = play_pause_seek.progress + 1
                    play_pause_seek.progress = progress
                }
            }
            postDelayed({ updateMediaPlayerProgress() }, amoungToUpdate)
        }
    }

    private fun onMediaPlayerCompleted() {
        onPauseButtonClicked()
        play_pause_seek.progress = 0
    }

    private fun startMediaPlayer() {
        if (isMediaPlayerPrepared) {
            setMediaPlayerProgress(play_pause_seek.progress)

            mediaPlayer.start()
            audioCapture.start()

            postDelayed({ updateMediaPlayerProgress() }, amoungToUpdate)
        }
    }

    private fun onPauseButtonClicked() {
        play_button.isVisible = true
        pause_button.isVisible = false
        record_button.isVisible = false
        stop_button.isVisible = false
        delete_button.isVisible = true
        horizontal_wave.isVisible = false
        play_pause_seek.isVisible = true

        audioCapture.stop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    private fun onFftListenerReceived(bytes: ByteArray?) {}

    ///////////////////////////////////////delete ////////////////////////////
    private fun onDeleteRecord() {
        play_button.isVisible = false
        pause_button.isVisible = false
        record_button.isVisible = true
        stop_button.isVisible = false
        delete_button.isVisible = false
        horizontal_wave.isVisible = true
        play_pause_seek.isVisible = false

        horizontal_wave.clearWave()

        if (isMediaPlayerPrepared) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            isMediaPlayerPrepared = false
        }
    }

    /////////////////////////timer///////////////////////
    private fun setTimer(seconds: Int) {
        val timerSeconds = seconds % 60
        val timerMinutes = (seconds - timerSeconds) / 60

        val secondsString = timerSeconds.toString().padStart(2, '0')
        val minutesString = timerMinutes.toString().padStart(2, '0')

        val timeFormatted = "$minutesString:$secondsString"
        timer_view.post {
            timer_view.text = timeFormatted
        }
    }

    /////////////////////////// seek progress ///////////////////////
    private fun setMediaPlayerProgress(progress: Int) {
        val seekTo = duration * (progress / 100f)
        mediaPlayer.seekTo(seekTo.toInt())
    }

}
