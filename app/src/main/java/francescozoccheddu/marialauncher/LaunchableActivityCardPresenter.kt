package francescozoccheddu.marialauncher

import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import android.view.ViewGroup

class LaunchableActivityCardPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = object : ImageCardView(parent.context) {}
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val activity = item as LaunchableActivity
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImage = activity.icon;
        cardView.titleText = activity.name
        cardView.contentText = activity.packageName;
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }

    companion object {
        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
    }

}