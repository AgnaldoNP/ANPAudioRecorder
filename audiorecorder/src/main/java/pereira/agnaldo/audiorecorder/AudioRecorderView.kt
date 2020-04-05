package pereira.agnaldo.audiorecorder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.isVisible
import com.github.windsekirun.naraeaudiorecorder.NaraeAudioRecorder
import com.github.windsekirun.naraeaudiorecorder.chunk.AudioChunk
import com.github.windsekirun.naraeaudiorecorder.config.AudioRecordConfig
import com.github.windsekirun.naraeaudiorecorder.model.RecordState
import com.github.windsekirun.naraeaudiorecorder.source.NoiseAudioSource
import kotlinx.android.synthetic.main.anp_ar_layout.view.*
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

    private var totalRecordedAudioDuration = 0L
    private var milliSecondsPerPercentage = 0L
    private var totalRecordedAudioDurationFormatted = "00:00"

    private var currentRecorderTime = 0L

    /**
     * TODO insert documentation
     */
    fun getAudioRecordedDuration() = totalRecordedAudioDuration

    /**
     * TODO insert documentation
     */
    fun getAudioRecorded() = recordFile

    /////////////////////// listeners ////////////////////////
    private var onStartRecordingListener: (() -> Unit)? = null
    private var onIStartRecordingListener: OnStartRecordingListener? = null

    /**
     * TODO insert documentation
     */
    fun setOnStartRecording(listener: () -> Unit) {
        onStartRecordingListener = listener
    }

    /**
     * TODO insert documentation
     */
    fun setOnStartRecording(listener: OnStartRecordingListener) {
        this.onIStartRecordingListener = listener
    }

    interface OnStartRecordingListener {
        fun onStartRecording()
    }


    private var onFinishRecordListener: ((File) -> Unit)? = null
    private var onIFinishRecordListener: OnFinishRecordListener? = null

    /**
     * TODO insert documentation
     */
    fun setOnFinishRecord(listener: (file: File) -> Unit) {
        onFinishRecordListener = listener
    }

    /**
     * TODO insert documentation
     */
    fun setOnFinishRecord(listener: OnFinishRecordListener) {
        this.onIFinishRecordListener = listener
    }

    interface OnFinishRecordListener {
        fun onFinishRecordListener(file: File)
    }


    private var onPlayListener: (() -> Unit)? = null
    private var onIPlayListener: OnPlayListener? = null

    /**
     * TODO insert documentation
     */
    fun setOnPlay(listener: () -> Unit) {
        onPlayListener = listener
    }

    /**
     * TODO insert documentation
     */
    fun setOnPlay(listener: OnPlayListener) {
        this.onIPlayListener = listener
    }

    interface OnPlayListener {
        fun onPlay()
    }


    private var onPauseListener: (() -> Unit)? = null
    private var onIPauseListener: OnPauseListener? = null

    /**
     * TODO insert documentation
     */
    fun setOnPause(listener: () -> Unit) {
        onPauseListener = listener
    }

    /**
     * TODO insert documentation
     */
    fun setOnPause(listener: OnPauseListener) {
        this.onIPauseListener = listener
    }

    interface OnPauseListener {
        fun onPause()
    }


    private var onResumeListener: (() -> Unit)? = null
    private var onIResumeListener: OnResumeListener? = null

    /**
     * TODO insert documentation
     */
    fun setOnResume(listener: () -> Unit) {
        onResumeListener = listener
    }

    /**
     * TODO insert documentation
     */
    fun setOnResume(listener: OnResumeListener) {
        this.onIResumeListener = listener
    }

    interface OnResumeListener {
        fun onResume()
    }


    private var onFinishPlayListener: (() -> Unit)? = null
    private var onIFinishPlayListener: OnFinishPlayListener? = null

    /**
     * TODO insert documentation
     */
    fun setOnFinishPlay(listener: () -> Unit) {
        onFinishPlayListener = listener
    }

    /**
     * TODO insert documentation
     */
    fun setOnFinishPlay(listener: OnFinishPlayListener) {
        this.onIFinishPlayListener = listener
    }

    interface OnFinishPlayListener {
        fun onFinishPlayListener()
    }


    private var onDeleteListener: (() -> Unit)? = null
    private var onIDeleteListener: OnDeleteListener? = null

    /**
     * TODO insert documentation
     */
    fun setOnDelete(listener: () -> Unit) {
        onDeleteListener = listener
    }

    /**
     * TODO insert documentation
     */
    fun setOnDelete(listener: OnDeleteListener) {
        this.onIDeleteListener = listener
    }

    interface OnDeleteListener {
        fun onDelete()
    }
    /////////////////////// end listeners ////////////////////////

    /////////////////// custom look and fell//////////////////////
    /**
     * TODO insert documentation
     */
    fun setRecordIcon(icon: Drawable?) {
        customRecordIcon = icon
    }

    /**
     * TODO insert documentation
     */
    fun setRecordIcon(icon: Bitmap?) {
        setRecordIcon(icon?.toDrawable(resources))
    }

    /**
     * TODO insert documentation
     */
    fun setPlayIcon(icon: Drawable?) {
        customPlayIcon = icon
    }

    /**
     * TODO insert documentation
     */
    fun setPlayIcon(icon: Bitmap?) {
        setPlayIcon(icon?.toDrawable(resources))
    }

    /**
     * TODO insert documentation
     */
    fun setPauseIcon(icon: Drawable?) {
        customPauseIcon = icon
    }

    /**
     * TODO insert documentation
     */
    fun setPauseIcon(icon: Bitmap?) {
        setPauseIcon(icon?.toDrawable(resources))
    }

    /**
     * TODO insert documentation
     */
    fun setStopIcon(icon: Drawable?) {
        customStopIcon = icon
    }

    /**
     * TODO insert documentation
     */
    fun setStopIcon(icon: Bitmap?) {
        setStopIcon(icon?.toDrawable(resources))
    }

    /**
     * TODO insert documentation
     */
    fun setDeleteIcon(icon: Drawable?) {
        customDeleteIcon = icon
    }

    /**
     * TODO insert documentation
     */
    fun setDeleteIcon(icon: Bitmap?) {
        setDeleteIcon(icon?.toDrawable(resources))
    }
    /////////////////// end custom look and fell//////////////////

    init {
        orientation = VERTICAL
        getStyles(attrs, defStyleAttr)
        post { loadCustomResources() }

        LayoutInflater.from(context).inflate(R.layout.anp_ar_layout, this)
        play_button.setOnClickListener { onPlayButtonClicked() }
        pause_button.setOnClickListener { onPauseButtonClicked() }
        stop_button.setOnClickListener { onStopButtonClicked() }
        record_button.setOnClickListener { onRecordButtonClicked() }
        delete_button.setOnClickListener { onDeleteButtonClicked() }

        play_pause_seek.isVisible = false
        timer_view_current.isVisible = false

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
        horizontal_wave.clearWave()
    }

    private fun initializeAudioRecorder() {
        val recordConfig = AudioRecordConfig.defaultConfig()
        val audioSource = NoiseAudioSource(recordConfig)

        recordFile.deleteIfExists()

        audioRecorder = NaraeAudioRecorder()
        audioRecorder.create {
            this.destFile = recordFile
            this.recordConfig = recordConfig
            this.audioSource = audioSource
            this.debugMode = true
            timerCountCallback = { currentTime, _ ->
                onTimerCountCallbackReceive(currentTime)
            }
            chunkAvailableCallback = { onAudioRecorderCallbackReceive(it) }
        }
        audioRecorder.setOnRecordStateChangeListener { recordState ->
            onRecordStateChangeListener(recordState)
        }
    }

    private var customRecordIcon: Drawable? = null
    private var customPlayIcon: Drawable? = null
    private var customPauseIcon: Drawable? = null
    private var customStopIcon: Drawable? = null
    private var customDeleteIcon: Drawable? = null
    private fun getStyles(attrs: AttributeSet?, defStyle: Int) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.AudioRecorderView, defStyle, R.style.defaultAudioRecorderView
            )
            customRecordIcon = typedArray.getDrawable(R.styleable.AudioRecorderView_recordIcon)
            customPlayIcon = typedArray.getDrawable(R.styleable.AudioRecorderView_playIcon)
            customPauseIcon = typedArray.getDrawable(R.styleable.AudioRecorderView_pauseIcon)
            customStopIcon = typedArray.getDrawable(R.styleable.AudioRecorderView_stopIcon)
            customDeleteIcon = typedArray.getDrawable(R.styleable.AudioRecorderView_deleteIcon)

            typedArray.recycle()
        }
    }

    private fun loadCustomResources() {
        customRecordIcon?.let { record_button.setImageDrawable(customRecordIcon) }
        customPlayIcon?.let { play_button.setImageDrawable(customPlayIcon) }
        customPauseIcon?.let { pause_button.setImageDrawable(customPauseIcon) }
        customStopIcon?.let { stop_button.setImageDrawable(customStopIcon) }
        customDeleteIcon?.let { delete_button.setImageDrawable(customDeleteIcon) }
    }

    ////////////////////////////////record/////////////////////////////
    private fun onRecordButtonClicked() {
        record_button

        play_button.isVisible = false
        pause_button.isVisible = false
        record_button.isVisible = false
        stop_button.isVisible = true
        delete_button.isVisible = false
        horizontal_wave.isVisible = true
        play_pause_seek.isVisible = false
        timer_view_current.isVisible = false

        if (!isRecording) {
            horizontal_wave.clearWave()
            initializeAudioRecorder()
            audioRecorder.startRecording(context)
        }

        onStartRecordingListener?.invoke()
        onIStartRecordingListener?.onStartRecording()
    }


    private fun onStopButtonClicked() {
        play_button.isVisible = true
        pause_button.isVisible = false
        record_button.isVisible = false
        stop_button.isVisible = false
        delete_button.isVisible = true
        horizontal_wave.isVisible = false
        play_pause_seek.isVisible = true
        timer_view_current.isVisible = true

        totalRecordedAudioDuration = currentRecorderTime
        if (isRecording) {
            isRecording = false
            audioRecorder.stopRecording()
            setMediaPlayerProgress(0)
            play_pause_seek.progress = 0
        }
    }

    private fun onAudioRecorderCallbackReceive(audioChunk: AudioChunk) {
        val byteArray = audioChunk.toByteArray()
        horizontal_wave.updateAudioWave(byteArray)
    }

    private fun onTimerCountCallbackReceive(currentTime: Long) {
        currentRecorderTime = currentTime
        setTimer((currentTime / 1000).toInt())
    }

    private fun onRecordStateChangeListener(recordState: RecordState) {
        isRecording = recordState == RecordState.START
        if (recordState == RecordState.STOP) {
            onFinishRecordListener?.invoke(recordFile)
            onIFinishRecordListener?.onFinishRecordListener(recordFile)

            totalRecordedAudioDuration = currentRecorderTime
            milliSecondsPerPercentage = totalRecordedAudioDuration / 100
            totalRecordedAudioDurationFormatted =
                getTimeFormatted((totalRecordedAudioDuration / 1000).toInt())
        }
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
        timer_view_current.isVisible = true

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
                    fis.close()
                } catch (ex1: Exception) {
                    ex1.printStackTrace()
                }
            }
        } else {
            startMediaPlayer()
        }
    }

    private fun onMediaPlayerPrepared() {
        totalRecordedAudioDuration = mediaPlayer.duration.toLong()
        milliSecondsPerPercentage = totalRecordedAudioDuration / 100
        totalRecordedAudioDurationFormatted =
            getTimeFormatted((totalRecordedAudioDuration / 1000).toInt())

        isMediaPlayerPrepared = true
        setMediaPlayerProgress(0)
        startMediaPlayer()
    }

    private fun updateMediaPlayerProgress() {
        if (mediaPlayer.isPlaying) {
            play_pause_seek.post {
                if (milliSecondsPerPercentage * play_pause_seek.progress < totalRecordedAudioDuration) {
                    setTimer((mediaPlayer.currentPosition / 1000))

                    val progress = play_pause_seek.progress + 1
                    play_pause_seek.progress = progress
                }
            }
            postDelayed({ updateMediaPlayerProgress() }, milliSecondsPerPercentage)
        }
    }

    private fun onMediaPlayerCompleted() {
        onPauseButtonClicked()
        play_pause_seek.progress = 0
        setMediaPlayerProgress(0)

        onFinishPlayListener?.invoke()
        onIFinishPlayListener?.onFinishPlayListener()
    }

    private fun startMediaPlayer() {
        if (isMediaPlayerPrepared) {
            if (play_pause_seek.progress == 0) {
                onPlayListener?.invoke()
                onIPlayListener?.onPlay()
            } else {
                onResumeListener?.invoke()
                onIResumeListener?.onResume()
            }

            setMediaPlayerProgress(play_pause_seek.progress)

            mediaPlayer.start()
            audioCapture.start()

            postDelayed({ updateMediaPlayerProgress() }, milliSecondsPerPercentage)
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
        timer_view_current.isVisible = true

        audioCapture.stop()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()

            onPauseListener?.invoke()
            onIPauseListener?.onPause()
        }
    }

    private fun onFftListenerReceived(bytes: ByteArray?) {}

    ///////////////////////////////////////delete ////////////////////////////
    private fun onDeleteButtonClicked() {
        play_button.isVisible = false
        pause_button.isVisible = false
        record_button.isVisible = true
        stop_button.isVisible = false
        delete_button.isVisible = false
        horizontal_wave.isVisible = true
        play_pause_seek.isVisible = false
        timer_view_current.isVisible = false

        horizontal_wave.clearWave()

        if (isMediaPlayerPrepared) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            isMediaPlayerPrepared = false
        }

        recordFile.deleteIfExists()

        isRecording = false
        totalRecordedAudioDuration = 0
        totalRecordedAudioDurationFormatted = "00:00"

        setMediaPlayerProgress(0)
        timer_view.post { timer_view.text = "00:00" }

        onDeleteListener?.invoke()
        onIDeleteListener?.onDelete()
    }

    /////////////////////////timer///////////////////////
    private fun setTimer(seconds: Int) {
        val currentTime = getTimeFormatted(seconds)
        if (!isMediaPlayerPrepared && isRecording) {
            timer_view.post { timer_view.text = currentTime }
            timer_view_current.setText(R.string.anp_ar_zero_time)

        } else {
            timer_view.post {
                timer_view_current.text = currentTime
            }
        }
    }

    private fun getTimeFormatted(seconds: Int): String {
        val timerSeconds = seconds % 60
        val timerMinutes = (seconds - timerSeconds) / 60

        val secondsString = timerSeconds.toString().padStart(2, '0')
        val minutesString = timerMinutes.toString().padStart(2, '0')

        return "$minutesString:$secondsString"
    }

    /////////////////////////// seek progress ///////////////////////
    private fun setMediaPlayerProgress(progress: Int) {
        val seekTo = totalRecordedAudioDuration * (progress / 100f)
        if (isMediaPlayerPrepared) {
            mediaPlayer.seekTo(seekTo.toInt())
        }
        setTimer((seekTo / 1000).toInt())
    }

}
