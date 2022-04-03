package francescozoccheddu.mmlauncher

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.leanback.widget.TitleViewAdapter

class HeaderView(context: Context?, attrs: AttributeSet?, defStyle: Int) :
    LinearLayout(context, attrs, defStyle), TitleViewAdapter.Provider {

    private val adapter: TitleViewAdapter = object : TitleViewAdapter() {

        override fun getSearchAffordanceView() = null

    }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    override fun getTitleViewAdapter() = adapter

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_header, this)
    }

}