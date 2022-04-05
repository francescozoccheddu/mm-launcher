package francescozoccheddu.mmlauncher

import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.leanback.widget.Presenter
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class TargetPresenter : Presenter() {

    class Holder(val root: View) : ViewHolder(root) {

        val label: TextView = root.findViewById(R.id.card_label)
        val icon: ImageView = root.findViewById(R.id.card_icon)

    }

    private lateinit var layoutParams: ViewGroup.LayoutParams

    fun updateCardHeight(resources: Resources) {
        val height = when (Prefs.cardSize) {
            Prefs.CardSize.VerySmall -> 50
            Prefs.CardSize.Small -> 75
            Prefs.CardSize.Medium -> 100
            Prefs.CardSize.Large -> 125
            Prefs.CardSize.VeryLarge -> 150
        }
        val size = sequenceOf(height / CARD_HW_RATIO, height).map {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                it.toFloat(),
                resources.displayMetrics
            ).roundToInt()
        }.toList()
        layoutParams = ViewGroup.LayoutParams(size[0], size[1])
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_card, parent, false)
        view.layoutParams = ViewGroup.LayoutParams(layoutParams)
        return Holder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val holder = viewHolder as Holder
        val target = item as Target
        if (target.banner != null) {
            holder.root.background = target.banner
            holder.icon.visibility = GONE
            holder.label.visibility = GONE
        } else {
            holder.root.setBackgroundColor(
                ContextCompat.getColor(
                    holder.root.context,
                    R.color.card_background
                )
            )
            if (item.icon != null) {
                holder.icon.setImageDrawable(item.icon)
                holder.icon.visibility = VISIBLE
                holder.label.visibility = GONE
            } else {
                holder.label.text = item.label
                holder.label.visibility = VISIBLE
                holder.icon.visibility = GONE
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val holder = viewHolder as Holder
        holder.root.background = null
        holder.icon.setImageDrawable(null)
    }

    companion object {
        private const val CARD_HW_RATIO = 0.5625
    }

}