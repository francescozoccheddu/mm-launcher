package francescozoccheddu.marialauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import java.util.*

data class Target(
    val packageName: String,
    val name: String,
    val label: String,
    val icon: Drawable
) {

    fun launch(context: Context) {
        val componentName = ComponentName(packageName, name)
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        intent.component = componentName
        context.startActivity(intent)
    }

    override fun equals(other: Any?) = other is Target
            && other.packageName == packageName
            && other.name == name

    override fun hashCode() = Objects.hash(packageName, name)

}