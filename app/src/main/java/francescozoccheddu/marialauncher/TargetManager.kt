package francescozoccheddu.marialauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object TargetManager {

    fun getTargets(context: Context): List<Target> {
        val packageManager: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val packages = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        return packages
            .mapNotNull { Target(it.activityInfo, packageManager) }
            .filter { PACKAGES.contains(it.packageName) }
    }

    private val PACKAGES = setOf("francescozoccheddu.marialauncher")

}