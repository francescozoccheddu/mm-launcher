package francescozoccheddu.mmlauncher

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.VerticalGridView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class MainActivity : FragmentActivity() {

    companion object {
        private const val MAX_TAP_DELAY: Long = 2000
        private const val TAPS_FOR_TOAST: Int = 5
        private const val TAPS_FOR_SETTINGS: Int = 10
        private const val MAX_WALLPAPER_SLIDE_FACTOR: Float = 0.2f
        private const val MAX_WALLPAPER_SLIDE_FACTOR_SCROLL_SCREEN_FACTOR: Float = 1f
    }

    private abstract inner class TapManager {

        private val handler = Handler(Looper.getMainLooper())
        protected var taps = 0
            private set
        private val resetter = Runnable {
            taps = 0
            updateRemainingTapsForSettings()
        }

        protected val remaining get() = TAPS_FOR_SETTINGS - taps

        protected abstract fun updateRemainingTapsForSettings()

        private fun launchSettings() {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        fun tap() {
            taps++
            if (taps >= TAPS_FOR_SETTINGS) {
                launchSettings()
            }
            handler.removeCallbacks(resetter)
            handler.postDelayed(resetter, MAX_TAP_DELAY)
            updateRemainingTapsForSettings()
        }

    }

    private inner class HelpTapManager : TapManager() {

        private val instructionsTextView: TextView = findViewById(R.id.help_instructions_text)

        init {
            updateRemainingTapsForSettings()
        }

        override fun updateRemainingTapsForSettings() {
            instructionsTextView.text = when (remaining) {
                0 -> resources.getString(R.string.help_instructions_done)
                TAPS_FOR_SETTINGS -> resources.getString(
                    R.string.help_instructions,
                    remaining
                )
                else -> resources.getQuantityString(
                    R.plurals.help_instructions_remaining,
                    remaining,
                    remaining
                )
            }
        }

    }

    private inner class ToastTapManager : TapManager() {

        private var toast: Toast? = null

        override fun updateRemainingTapsForSettings() {
            toast?.cancel()
            if (taps >= TAPS_FOR_TOAST && remaining > 0) {
                val message = resources.getQuantityString(
                    R.plurals.toast_settings_remaining,
                    remaining,
                    remaining
                )
                toast = Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT)
                toast!!.show()
            }
        }

    }

    private inner class GridManager {

        init {
            findViewById<View>(R.id.main_grid_fragment)
                .startAnimation(
                    AnimationUtils.loadAnimation(
                        this@MainActivity,
                        R.anim.wallpaper_start
                    )
                )
        }

        val rowCount = ceil(Prefs.activeTargets.size.toDouble() / Prefs.columnCount).toInt()

        fun setup() {
            val fragment =
                supportFragmentManager.findFragmentById(R.id.main_grid_fragment) as GridFragment
            val view: VerticalGridView = fragment.requireView()
                .findViewById(androidx.leanback.R.id.browse_grid)
            val position = IntArray(2)
            view.setOnScrollChangeListener { _, _, _, _, _ ->
                val firstChild = view.getChildAt(0)!!
                firstChild.getLocationOnScreen(position)
                val firstChildTop = position[1]
                headerManager.gridTop = firstChildTop
            }
            var scrollY = 0
            view.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val childHeight = view.getChildAt(0)!!.measuredHeight
                    val spacing = view.verticalSpacing
                    val height = childHeight * rowCount + spacing * (rowCount - 1)
                    val fullHeight = height + view.paddingBottom + view.paddingTop
                    val scrollHeight = max(fullHeight - resources.displayMetrics.heightPixels, 0)
                    scrollY += dy
                    wallpaperManager.scrollHeight = scrollHeight
                    wallpaperManager.progress =
                        if (scrollHeight == 0) 0.0
                        else scrollY.toDouble() / scrollHeight
                }

            })
        }

    }

    private inner class HeaderManager {

        private val showAnimation =
            AnimationUtils.loadAnimation(this@MainActivity, R.anim.header_show)
        private val hideAnimation =
            AnimationUtils.loadAnimation(this@MainActivity, R.anim.header_hide)
        private var wasOverlapping: Boolean = false
        private var started: Boolean = false
        private val view: ViewGroup = findViewById(R.id.header)
        private val childPanelView: ViewGroup = view.findViewById(R.id.header_child_panel)
        private val position = IntArray(2)

        fun update() {
            val actualGridTop = gridTop ?: return
            view.getLocationOnScreen(position)
            val headerBottom = position[1] + view.measuredHeight
            childPanelView.translationY = min(actualGridTop - headerBottom, 0).toFloat()
            val overlaps = actualGridTop < headerBottom
            if (!overlaps && !started) {
                started = true
                view.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@MainActivity,
                        R.anim.header_start
                    )
                )
            } else if (overlaps != wasOverlapping) {
                wasOverlapping = overlaps
                view.clearAnimation()
                view.startAnimation(if (overlaps) hideAnimation else showAnimation)
            }
        }

        var gridTop: Int? = null
            set(value) {
                if (value == field) {
                    return
                }
                field = value
                update()
            }

    }

    private inner class WallpaperManager {

        private val imageView: ImageView = findViewById(R.id.main_wallpaper)

        init {
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.wallpaper)
            imageView.setImageBitmap(bitmap)
            imageView.startAnimation(
                AnimationUtils.loadAnimation(
                    this@MainActivity,
                    R.anim.wallpaper_start
                )
            )
            updateScreenAndImage()
        }

        private var maxTranslation: Double = 0.0
        private var scale: Double = 0.0

        fun updateScreenAndImage() {
            val imageWidth = imageView.drawable.intrinsicWidth
            val imageHeight = imageView.drawable.intrinsicHeight
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            val fitScale = run {
                val fitScaleX = screenWidth.toDouble() / imageWidth
                val fitScaleY = screenHeight.toDouble() / imageHeight
                max(fitScaleX, fitScaleY)
            }
            val slideFactor = run {
                val scrollScreenFactor = scrollHeight.toDouble() / screenHeight
                MAX_WALLPAPER_SLIDE_FACTOR * min(
                    scrollScreenFactor / MAX_WALLPAPER_SLIDE_FACTOR_SCROLL_SCREEN_FACTOR,
                    1.0
                )
            }
            scale = fitScale * (1.0 + slideFactor)
            maxTranslation = screenHeight * slideFactor
            updateProgress()
        }

        var scrollHeight = 0
            set(value) {
                if (value == field) {
                    return
                }
                field = value
                updateScreenAndImage()
            }

        fun updateProgress() {
            imageView.imageMatrix = Matrix().apply {
                setScale(
                    scale.toFloat(),
                    scale.toFloat()
                )
                postTranslate(0.0f, (-progress * maxTranslation).toFloat())
            }
        }

        var progress: Double = 0.0
            set(value) {
                val clampedValue = min(max(value, 0.0), 1.0)
                if (clampedValue == field) {
                    return
                }
                field = clampedValue
                updateProgress()
            }

    }

    private lateinit var tapManager: TapManager
    private lateinit var headerManager: HeaderManager
    private lateinit var wallpaperManager: WallpaperManager
    private var gridManager: GridManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.update(this)
        val hasTargets = Prefs.activeTargets.isNotEmpty()
        setContentView(R.layout.activity_main)
        if (hasTargets) {
            findViewById<ViewStub>(R.id.main_header_stub).inflate()
            gridManager = GridManager()
            headerManager = HeaderManager()
            tapManager = ToastTapManager()
        } else {
            findViewById<ViewStub>(R.id.main_help_stub).inflate()
            tapManager = HelpTapManager()
        }
        wallpaperManager = WallpaperManager()
    }

    override fun onResume() {
        super.onResume()
        gridManager?.setup()
    }

    override fun onBackPressed() {
        tapManager.tap()
    }

}