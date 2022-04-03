package francescozoccheddu.marialauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import java.util.*

class Target(
    private val activityInfo: ActivityInfo,
    private val packageManager: PackageManager
) {

    val packageName: String
        get() = activityInfo.packageName

    val name: String
        get() = activityInfo.name

    val label: String by lazy { activityInfo.loadLabel(packageManager).toString() }

    val icon: Drawable? by lazy {
        activityInfo.loadIcon(packageManager)
            ?: activityInfo.loadLogo(packageManager)
            ?: activityInfo.loadUnbadgedIcon(packageManager)
    }

    val banner: Drawable? by lazy { activityInfo.loadBanner(packageManager) }

    fun launch(context: Context) {
        val componentName = ComponentName(packageName, name)
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        intent.component = componentName
        context.startActivity(intent)
    }

    override fun equals(other: Any?) = other is Target && other.name == name

    override fun hashCode() = Objects.hashCode(name)

}