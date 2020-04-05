package pereira.agnaldo.audiorecorder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

@SuppressLint("RtlHardcoded")
class HorizontalWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mCurrentPath = Path()
    private val mCurrentPaint = Paint()
    private val mBackgroundPaint = Paint()
    internal var bytesSampleCount = 12

    fun setBytesSampleCount(bytesSampleCount: Int) {
        this.bytesSampleCount = bytesSampleCount
        clearWave()
    }

    fun getBytesSampleCount(): Int = this.bytesSampleCount

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        mBackgroundPaint.style = Paint.Style.STROKE
        mBackgroundPaint.color = Color.TRANSPARENT

        mCurrentPaint.style = Paint.Style.STROKE
        mCurrentPaint.color = resources.getColor(R.color.anp_ar_wave_color)
        mCurrentPaint.strokeWidth = 4f
        mCurrentPaint.alpha = 200

        clearWave()
    }

    fun updateAudioWave(audioData: ByteArray) {
        createAudioWave(audioData)
        invalidate()
    }

    fun clearWave() {
        post {
            mCurrentPath.reset()
            updateAudioWave(Helper.getBytesSampleWithZeros(bytesSampleCount))
        }
    }

    private fun createAudioWave(audioData: ByteArray) {
        val middleHeight = (height / 2).toFloat()

        val width = width.toFloat()
        val semiPeriod = width / (bytesSampleCount * 2)
        val norm = (height / 2).toFloat() / 165

        mCurrentPath.reset()
        mCurrentPath.moveTo(0f, middleHeight)
        var lastX = 0f
        var lastY = middleHeight

        for (i in 1 until (bytesSampleCount * 2)) {
            val byteIndice =
                if (i < bytesSampleCount) bytesSampleCount - i else i - bytesSampleCount
            val aData = audioData[byteIndice]
            val y = middleHeight + aData * norm
            val x = i * semiPeriod
            mCurrentPath.quadTo(
                lastX, lastY,
                ((x + lastX) / 2),
                ((y + lastY) / 2)
            )
            lastX = x
            lastY = y
        }

        mCurrentPath.quadTo(
            lastX, lastY,
            width,
            middleHeight
        )

        mCurrentPath.lineTo(width, middleHeight)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPaint(mBackgroundPaint)
        canvas.drawPath(mCurrentPath, mCurrentPaint)
    }

}
