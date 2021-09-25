package com.misaengfly.chordbox.visualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.misaengfly.chordbox.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

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
    protected var maxAmp = 10000f // max AMP에 따라서 바 크기가 결정됨
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

    private lateinit var tickTimeMarkColor: Paint
    private var tickBarWidth = 1f

    private lateinit var tickTimeMarkString: Paint

    private fun init() {
        backgroundBarPrimeColor = Paint() // 백그라운드에 그려질 Bar
        this.backgroundBarPrimeColor.color = context.getColorCompat(R.color.gray)
        this.backgroundBarPrimeColor.strokeCap = Paint.Cap.ROUND
        this.backgroundBarPrimeColor.strokeWidth = barWidth

        loadedBarPrimeColor = Paint() // 포어그라운드에 그려질 Bar
        this.loadedBarPrimeColor.color = context.getColorCompat(R.color.colorSecondary)
        this.loadedBarPrimeColor.strokeCap = Paint.Cap.ROUND
        this.loadedBarPrimeColor.strokeWidth = barWidth

        tickTimeMarkColor = Paint() // 아래에 초 표시줄
        this.tickTimeMarkColor.color = context.getColorCompat(R.color.white)
        this.tickTimeMarkColor.strokeWidth = tickBarWidth

        tickTimeMarkString = Paint()
        this.tickTimeMarkString.color = context.getColorCompat(R.color.white)
        this.tickTimeMarkString.textSize = 120f
    }

    private fun loadAttribute(context: Context, attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.visualizer, 0, 0
        )
        try {
            // bar 사이의 거리
            spaceBetweenBar =
                typedArray.getDimension(R.styleable.visualizer_spaceBetweenBar, context.dpToPx(2f))
            // 대략적인 막대 지속 시간
            approximateBarDuration =
                typedArray.getInt(R.styleable.visualizer_approximateBarDuration, 50)

            barWidth = typedArray.getDimension(R.styleable.visualizer_barWidth, 2f)
            // 소리 max 값을 설정하여 바 높이 조절 가능
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

            tickTimeMarkColor.apply {
                strokeWidth = tickBarWidth
                color = typedArray.getColor(
                    R.styleable.visualizer_tickMarkColor,
                    context.getColorCompat(R.color.white)
                )
            }

        } finally {
            typedArray.recycle()
        }
    }

    protected val currentDuration: Long
        get() = getTimeStamp(cursorPosition)

    // Time Line - 1초
    private val tick = approximateBarDuration / (tickDuration * 2)

    override fun onDraw(canvas: Canvas) {
        if (amps.isNotEmpty()) {
            for (i in getStartBar() until getEndBar()) {
                val startX = width / 2 - (getBarPosition() - i) * (barWidth + spaceBetweenBar)
                drawStraightBar(canvas, startX, getBarHeightAt(i).toInt(), getBaseLine())

                if (i % tick == 0) {
                    drawBottomBar(canvas, startX, getBarHeightAt(i).toInt(), getBaseLine())
                    drawBottomNumber(
                        canvas,
                        startX,
                        getBaseLine(),
                        (i / tick).toString()
                    )
                }
            }
        }
        super.onDraw(canvas)
    }

    private fun drawBottomBar(canvas: Canvas, startX: Float, height: Int, baseLine: Int) {
        val bottom = baseLine * 2
        canvas.drawLine(
            startX,
            bottom.toFloat(),
            startX,
            bottom.toFloat() - 100f,
            tickTimeMarkColor
        )
    }

    private fun drawBottomNumber(
        canvas: Canvas,
        startX: Float,
        baseLine: Int,
        number: String
    ) {
        val bottom = baseLine * 2
        canvas.drawText(number, startX, bottom.toFloat() - 100f, tickTimeMarkString)
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

    private fun getBaseLine() = height / 2 // View heightd의 1/2
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