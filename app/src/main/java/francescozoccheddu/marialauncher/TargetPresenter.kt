package francescozoccheddu.marialauncher

import android.content.Context
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
import androidx.preference.PreferenceManager
import kotlin.math.roundToInt

class TargetPresenter : Presenter() {

    class Holder(val root: View) : ViewHolder(root) {

        val label: TextView = root.findViewById(R.id.card_label)
        val icon: ImageView = root.findViewById(R.id.card_icon)

    }

    private lateinit var layoutParams: ViewGroup.LayoutParams

    fun updateCardHeight(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val height = when (prefs.getString(
            context.resources.getString(R.string.pref_card_size_key),
            context.resources.getInteger(R.integer.pref_card_size_default).toString()
        )!!.toInt()) {
            0 -> 50
            1 -> 75
            3 -> 125
            4 -> 150
            else -> 100
        }
        val size = sequenceOf(height / CARD_HW_RATIO, height).map {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                it.toFloat(),
                context.resources.displayMetrics
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