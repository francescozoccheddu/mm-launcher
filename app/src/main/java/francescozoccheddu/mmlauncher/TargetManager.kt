package francescozoccheddu.mmlauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object TargetManager {

    fun getAllTargets(context: Context): Set<Target> {
        val packageManager: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val packages = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        val thisPackageName = context.applicationContext.packageName
        return packages
            .mapNotNull { Target(it.activityInfo, packageManager) }
            .filter { it.packageName != thisPackageName }
            .toSortedSet(compareBy { it.label })
    }

}