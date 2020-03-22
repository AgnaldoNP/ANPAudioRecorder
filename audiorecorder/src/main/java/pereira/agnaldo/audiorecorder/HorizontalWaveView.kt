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

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        mBackgroundPaint.style = Paint.Style.STROKE
        mBackgroundPaint.color = Color.TRANSPARENT

        mCurrentPaint.style = Paint.Style.STROKE
        mCurrentPaint.color = resources.getColor(R.color.anp_base_color)
        mCurrentPaint.strokeWidth = 4f
        mCurrentPaint.alpha = 200

    }

    fun updateAudioWave(audioData: ByteArray) {
        createAudioWave(audioData)
        invalidate()
    }

    fun clearWave() {
        mCurrentPath.reset()
        invalidate()
    }

    private fun createAudioWave(audioData: ByteArray) {
        val middleHeigth = (height / 2).toFloat()

        val byteCount = 12
        val width = width.toFloat()
        val semiPeriod = width / (byteCount * 2)
        val norm = (height / 2).toFloat() / 165

        mCurrentPath.reset()
        mCurrentPath.moveTo(0f, middleHeigth)
        var lastX = 0f
        var lastY = middleHeigth

        for (i in 1 until (byteCount * 2)) {
            val byteIndice = if (i < byteCount) byteCount - i else i - byteCount
            val aData = audioData[byteIndice]
            val y = middleHeigth + aData * norm
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
            middleHeigth
        )

        mCurrentPath.lineTo(width, middleHeigth)
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPaint(mBackgroundPaint)
        canvas.drawPath(mCurrentPath, mCurrentPaint)
    }

}
