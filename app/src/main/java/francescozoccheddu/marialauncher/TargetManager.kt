package francescozoccheddu.marialauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager


object TargetManager {

    lateinit var allTargets : List<Target>
        get
        private set

    lateinit var activeTargets : List<Target>
        get
        private set

    fun setActive(target: Target, boolean: Boolean) {
        TODO()
    }

    fun update(context: Context) {
        val packageManager: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        //intent.addCategory(Intent.CATEGORY_LAUNCHER)
        // FIXME
        intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val packages = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        val thisPackageName = context.applicationContext.packageName
        allTargets = packages
            .mapNotNull { Target(it.activityInfo, packageManager) }
            .filter { it.packageName != thisPackageName }
        activeTargets = allTargets // for now
    }

}