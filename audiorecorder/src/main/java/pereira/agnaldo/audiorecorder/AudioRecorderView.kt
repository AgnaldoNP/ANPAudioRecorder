package pereira.agnaldo.audiorecorder

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
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
import pereira.agnaldo.audiorecorder.Helper.Companion.lightenColor
import java.io.File
import java.io.FileInputStream

@Suppress("MemberVisibilityCanBePrivate")
class AudioRecorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val mediaPlayer = MediaPlayer()
    private var isMediaPlayerPrepared = false

    private lateinit var audioRecorder: NaraeAudioRecorder
    private var recordFile: File
    private var isRecording = false

    private var totalRecordedAudioDuration = 0L
    private var milliSecondsPerPercentage = 0L
    private var totalRecordedAudioDurationFormatted = "00:00"

    private var currentRecorderTime = 0L

    fun getAudioRecordedDuration() = totalRecordedAudioDuration

    fun getAudioRecorded() = recordFile

    /////////////////////// listeners ////////////////////////
    private var onStartRecordingListener: (() -> Unit)? = null
    private var onIStartRecordingListener: OnStartRecordingListener? = null

    fun setOnStartRecording(listener: () -> Unit) {
        onStartRecordingListener = listener
    }

    fun setOnStartRecording(listener: OnStartRecordingListener) {
        this.onIStartRecordingListener = listener
    }

    interface OnStartRecordingListener {
        fun onStartRecording()
    }


    private var onFinishRecordListener: ((File) -> Unit)? = null
    private var onIFinishRecordListener: OnFinishRecordListener? = null

    fun setOnFinishRecord(listener: (file: File) -> Unit) {
        onFinishRecordListener = listener
    }

    fun setOnFinishRecord(listener: OnFinishRecordListener) {
        this.onIFinishRecordListener = listener
    }

    interface OnFinishRecordListener {
        fun onFinishRecordListener(file: File)
    }


    private var onPlayListener: (() -> Unit)? = null
    private var onIPlayListener: OnPlayListener? = null

    fun setOnPlay(listener: () -> Unit) {
        onPlayListener = listener
    }

    fun setOnPlay(listener: OnPlayListener) {
        this.onIPlayListener = listener
    }

    interface OnPlayListener {
        fun onPlay()
    }


    private var onPauseListener: (() -> Unit)? = null
    private var onIPauseListener: OnPauseListener? = null

    fun setOnPause(listener: () -> Unit) {
        onPauseListener = listener
    }

    fun setOnPause(listener: OnPauseListener) {
        this.onIPauseListener = listener
    }

    interface OnPauseListener {
        fun onPause()
    }


    private var onResumeListener: (() -> Unit)? = null
    private var onIResumeListener: OnResumeListener? = null

    fun setOnResume(listener: () -> Unit) {
        onResumeListener = listener
    }

    fun setOnResume(listener: OnResumeListener) {
        this.onIResumeListener = listener
    }

    interface OnResumeListener {
        fun onResume()
    }


    private var onFinishPlayListener: (() -> Unit)? = null
    private var onIFinishPlayListener: OnFinishPlayListener? = null

    fun setOnFinishPlay(listener: () -> Unit) {
        onFinishPlayListener = listener
    }

    fun setOnFinishPlay(listener: OnFinishPlayListener) {
        this.onIFinishPlayListener = listener
    }

    interface OnFinishPlayListener {
        fun onFinishPlayListener()
    }


    private var onDeleteListener: (() -> Unit)? = null
    private var onIDeleteListener: OnDeleteListener? = null

    fun setOnDelete(listener: () -> Unit) {
        onDeleteListener = listener
    }

    fun setOnDelete(listener: OnDeleteListener) {
        this.onIDeleteListener = listener
    }

    interface OnDeleteListener {
        fun onDelete()
    }
    /////////////////////// end listeners ////////////////////////

    /////////////////// custom look and fell//////////////////////
    fun setRecordIcon(icon: Drawable?) {
        customRecordIcon = icon
    }

    fun setRecordIcon(icon: Bitmap?) {
        setRecordIcon(icon?.toDrawable(resources))
    }

    fun setPlayIcon(icon: Drawable?) {
        customPlayIcon = icon
    }

    fun setPlayIcon(icon: Bitmap?) {
        setPlayIcon(icon?.toDrawable(resources))
    }

    fun setPauseIcon(icon: Drawable?) {
        customPauseIcon = icon
    }

    fun setPauseIcon(icon: Bitmap?) {
        setPauseIcon(icon?.toDrawable(resources))
    }

    fun setStopIcon(icon: Drawable?) {
        customStopIcon = icon
    }

    fun setStopIcon(icon: Bitmap?) {
        setStopIcon(icon?.toDrawable(resources))
    }

    fun setDeleteIcon(icon: Drawable?) {
        customDeleteIcon = icon
    }

    fun setDeleteIcon(icon: Bitmap?) {
        setDeleteIcon(icon?.toDrawable(resources))
    }
    /////////////////// end custom look and fell//////////////////

    init {
        orientation = VERTICAL
        getStyles(attrs, defStyleAttr)
        post { loadCustomResources() }

        recordFile = File(context.cacheDir, "record_${System.currentTimeMillis()}.wav")

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
    private var customBackground: Drawable? = null
    private var customBaseColor: Int = 0

    private var customRecordIconTint: Int = 0
    private var customPlayIconTint: Int = 0
    private var customPauseIconTint: Int = 0
    private var customStopIconTint: Int = 0
    private var customDeleteIconTint: Int = 0

    private var customWaveTint: Int = 0
    private var customProgressTint: Int = 0
    private var customBackgroundTint: Int = 0

    private var customTimeTint: Int = 0
    private var customTotalTimeTint: Int = 0
    private var customCurrentTimeTint: Int = 0

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
            customBackground = typedArray.getDrawable(R.styleable.AudioRecorderView_background)
            customBaseColor = typedArray.getColor(R.styleable.AudioRecorderView_baseColor, 0)

            customRecordIconTint =
                typedArray.getColor(R.styleable.AudioRecorderView_recordIconTint, 0)
            customPlayIconTint = typedArray.getColor(R.styleable.AudioRecorderView_playIconTint, 0)
            customPauseIconTint =
                typedArray.getColor(R.styleable.AudioRecorderView_pauseIconTint, 0)
            customStopIconTint = typedArray.getColor(R.styleable.AudioRecorderView_stopIconTint, 0)
            customDeleteIconTint =
                typedArray.getColor(R.styleable.AudioRecorderView_deleteIconTint, 0)
            customWaveTint =
                typedArray.getColor(R.styleable.AudioRecorderView_recordWaveTint, 0)
            customProgressTint =
                typedArray.getColor(R.styleable.AudioRecorderView_playProgressTint, 0)
            customBackgroundTint =
                typedArray.getColor(R.styleable.AudioRecorderView_backgroundTint, 0)

            customTimeTint = typedArray.getColor(R.styleable.AudioRecorderView_timeTint, 0)
            customTotalTimeTint =
                typedArray.getColor(R.styleable.AudioRecorderView_totalTimeTint, 0)
            customCurrentTimeTint =
                typedArray.getColor(R.styleable.AudioRecorderView_currentTimeTint, 0)

            typedArray.recycle()
        }
    }

    private fun loadCustomResources() {
        customRecordIcon?.let { record_button.setImageDrawable(customRecordIcon) }
        customPlayIcon?.let { play_button.setImageDrawable(customPlayIcon) }
        customPauseIcon?.let { pause_button.setImageDrawable(customPauseIcon) }
        customStopIcon?.let { stop_button.setImageDrawable(customStopIcon) }
        customDeleteIcon?.let { delete_button.setImageDrawable(customDeleteIcon) }
        customBackground?.let { anp_ar_component_layout.background = customBackground }

        if (customBaseColor != 0) {
            record_button.setColorFilter(customBaseColor)
            play_button.setColorFilter(customBaseColor)
            pause_button.setColorFilter(customBaseColor)
            stop_button.setColorFilter(customBaseColor)

            val customBaseColorLight = lightenColor(customBaseColor, 0.2F)
            val customBaseColorLighter = lightenColor(customBaseColor, 0.22F)

            timer_view.setTextColor(customBaseColorLight)
            timer_view_current.setTextColor(customBaseColorLight)
            horizontal_wave.setWaveColor(customBaseColorLight)
            delete_button.setColorFilter(customBaseColorLighter)

            play_pause_seek.apply {
                thumb.colorFilter = PorterDuffColorFilter(customBaseColor, PorterDuff.Mode.SRC_ATOP)
                progressDrawable.colorFilter =
                    PorterDuffColorFilter(customBaseColor, PorterDuff.Mode.SRC_ATOP)
            }

            if (customBackground == null) {
                anp_ar_component_layout.background?.colorFilter =
                    PorterDuffColorFilter(customBaseColor, PorterDuff.Mode.SRC_ATOP)
            }
        }

        if (customRecordIconTint != 0) {
            record_button.setColorFilter(customRecordIconTint)
        }
        if (customPlayIconTint != 0) {
            play_button.setColorFilter(customPlayIconTint)
        }
        if (customPauseIconTint != 0) {
            pause_button.setColorFilter(customPauseIconTint)
        }
        if (customStopIconTint != 0) {
            stop_button.setColorFilter(customStopIconTint)
        }
        if (customDeleteIconTint != 0) {
            delete_button.setColorFilter(customDeleteIconTint)
        }

        if (customWaveTint != 0) {
            horizontal_wave.setWaveColor(customWaveTint)
        }
        if (customBackgroundTint != 0) {
            anp_ar_component_layout.background?.colorFilter =
                PorterDuffColorFilter(customBackgroundTint, PorterDuff.Mode.SRC_ATOP)
        }
        if (customProgressTint != 0) {
            play_pause_seek.apply {
                thumb.colorFilter =
                    PorterDuffColorFilter(customProgressTint, PorterDuff.Mode.SRC_ATOP)
                progressDrawable.colorFilter =
                    PorterDuffColorFilter(customProgressTint, PorterDuff.Mode.SRC_ATOP)
            }
        }

        if (customTimeTint != 0) {
            timer_view_current.setTextColor(customTimeTint)
            timer_view.setTextColor(customTimeTint)
        }
        if (customTotalTimeTint != 0) {
            timer_view.setTextColor(customTotalTimeTint)
        }
        if (customCurrentTimeTint != 0) {
            timer_view_current.setTextColor(customCurrentTimeTint)
        }
    }

    ////////////////////////////////record/////////////////////////////
    private fun onRecordButtonClicked() {
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

        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()

            onPauseListener?.invoke()
            onIPauseListener?.onPause()
        }
    }

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
