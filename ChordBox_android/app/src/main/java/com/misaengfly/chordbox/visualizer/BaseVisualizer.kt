package com.misaengfly.chordbox.visualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.misaengfly.chordbox.R
import com.misaengfly.chordbox.record.dpToPx
import com.misaengfly.chordbox.record.getColorCompat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.reflect.jvm.internal.impl.utils.NumberWithRadix

open class BaseVisualizer : View {

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
    ) : super(context, attrs) {
        init()
        loadAttribute(context, attrs)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr) {
        init()
        loadAttribute(context, attrs)
    }

    var ampNormalizer: (Int) -> Int = { sqrt(it.toFloat()).toInt() }

    protected var amps = mutableListOf<Int>()
    protected var maxAmp = 10000f
    protected var approximateBarDuration = 50
    protected var spaceBetweenBar = 2f
    protected var cursorPosition = 0f
    protected var tickPerBar = 1
    protected var tickDuration = 1
    protected var tickCount = 0
    protected var barDuration = 1000
    protected var barWidth = 2f
        set(value) {
            if (field > 0) {
                field = value
                this.backgroundBarPrimeColor.strokeWidth = value
                this.loadedBarPrimeColor.strokeWidth = value
            }
        }
    private var maxVisibleBars = 0
    private lateinit var loadedBarPrimeColor: Paint
    private lateinit var backgroundBarPrimeColor: Paint

    /**
     * 코드 그리기 위해 필요한 프로퍼티
     **/
    private lateinit var bottomBarPaint: Paint
    private lateinit var bottomTextPaint: Paint
    private var bottomIdx: Int = 0

    protected var timeStampDrawable: Boolean = false
    protected lateinit var chordDrawMap: Map<Int, String>

    private fun init() {
        backgroundBarPrimeColor = Paint()
        this.backgroundBarPrimeColor.color = context.getColorCompat(R.color.gray)
        this.backgroundBarPrimeColor.strokeCap = Paint.Cap.ROUND
        this.backgroundBarPrimeColor.strokeWidth = barWidth

        loadedBarPrimeColor = Paint()
        this.loadedBarPrimeColor.color = context.getColorCompat(R.color.colorSecondary)
        this.loadedBarPrimeColor.strokeCap = Paint.Cap.ROUND
        this.loadedBarPrimeColor.strokeWidth = barWidth

        bottomBarPaint = Paint()
        this.bottomBarPaint.color = context.getColorCompat(R.color.white)
        this.bottomBarPaint.strokeWidth = barWidth

        bottomTextPaint = Paint()
        this.bottomTextPaint.color = context.getColorCompat(R.color.white)
        this.bottomTextPaint.textSize = 90f

        bottomIdx = 0
    }

    private fun loadAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.visualizer, 0, 0
        )
        try {
            spaceBetweenBar =
                typedArray.getDimension(R.styleable.visualizer_spaceBetweenBar, context.dpToPx(2f))
            approximateBarDuration =
                typedArray.getInt(R.styleable.visualizer_approximateBarDuration, 50)

            barWidth = typedArray.getDimension(R.styleable.visualizer_barWidth, 2f)
            maxAmp = typedArray.getFloat(R.styleable.visualizer_maxAmp, 50f)

            loadedBarPrimeColor.apply {
                strokeWidth = barWidth
                color = typedArray.getColor(
                    R.styleable.visualizer_loadedBarPrimeColor,
                    context.getColorCompat(R.color.colorSecondary)
                )
            }
            backgroundBarPrimeColor.apply {
                strokeWidth = barWidth
                color = typedArray.getColor(
                    R.styleable.visualizer_backgroundBarPrimeColor,
                    context.getColorCompat(R.color.gray)
                )
            }

        } finally {
            typedArray.recycle()
        }
    }

    protected val currentDuration: Long
        get() = getTimeStamp(cursorPosition)

    override fun onDraw(canvas: Canvas) {
        if (amps.isNotEmpty()) {

            // 그려줘야 할 코드 체크하는 용도
            val drawCheck = Array((getEndBar() / 40 + 1) * 10) { false }

            val mtickDuration = tickDuration / 10

            for (i in getStartBar() until getEndBar()) {
                val startX = width / 2 - (getBarPosition() - i) * (barWidth + spaceBetweenBar)
                drawStraightBar(canvas, startX, getBarHeightAt(i).toInt(), getBaseLine())

//                if (timeStampDrawable && !drawCheck[(i / tickDuration)]) {
                if (timeStampDrawable && !drawCheck[(i / mtickDuration)]) {
                    // 코드 뒤로 밀리는것 방지
//                    if ((i % tickDuration) > 2) {
                    if ((i % mtickDuration) > 2) {
//                        drawCheck[(i / tickDuration)] = true
                        drawCheck[(i / mtickDuration)] = true
                        continue
                    }

//                    bottomIdx = (i / tickDuration)
                    bottomIdx = (i / mtickDuration)
                    val timeBaseLine = (getBaseLine() * 2).toFloat()
                    canvas.drawLine(
                        startX,
                        timeBaseLine,
                        startX,
                        timeBaseLine - 50f,
                        bottomBarPaint
                    )


                    if (chordDrawMap.containsKey(bottomIdx)) {
                        canvas.drawText(
                            chordDrawMap[bottomIdx]!!,
                            startX - 50f,
                            timeBaseLine - 60f,
                            bottomTextPaint
                        )
                    }
                    drawCheck[bottomIdx] = true
                }
            }
        }
        super.onDraw(canvas)
    }

    private fun drawStraightBar(canvas: Canvas, startX: Float, height: Int, baseLine: Int) {
        val startY = baseLine + (height / 2).toFloat()
        val stopY = startY - height
        if (startX <= width / 2) {
            canvas.drawLine(startX, startY, startX, stopY, loadedBarPrimeColor)
        } else {
            canvas.drawLine(startX, startY, startX, stopY, backgroundBarPrimeColor)
        }
    }

    private fun getBaseLine() = height / 2
    private fun getStartBar() = max(0, getBarPosition().toInt() - maxVisibleBars / 2)
    private fun getEndBar() = min(amps.size, getStartBar() + maxVisibleBars)
    private fun getBarHeightAt(i: Int) = height * max(0.01f, min(amps[i] / maxAmp, 0.9f))
    private fun getBarPosition() = cursorPosition / tickPerBar.toFloat()
    private fun inRangePosition(position: Float) = min(tickCount.toFloat(), max(0f, position))
    protected fun getTimeStamp(position: Float) = (position.toLong() * tickDuration)
    protected fun calculateCursorPosition(currentTime: Long) =
        inRangePosition(currentTime / tickDuration.toFloat())

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        maxVisibleBars = (width / (barWidth + spaceBetweenBar)).toInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxVisibleBars = (w / (barWidth + spaceBetweenBar)).toInt()
    }

    override fun onDetachedFromWindow() {
        ampNormalizer = { 0 }
        super.onDetachedFromWindow()
    }
}