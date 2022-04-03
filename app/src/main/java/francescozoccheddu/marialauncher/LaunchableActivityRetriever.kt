package francescozoccheddu.marialauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager


object LaunchableActivityRetriever {

    fun getLaunchableActivities(context: Context) : List<LaunchableActivity> {
        val packageManager: PackageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        // FIXME
        //intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val packages = packageManager.queryIntentActivities(intent, PackageManager.GET_META_DATA)
        return packages.mapNotNull {
            it.activityInfo.packageName
            LaunchableActivity(
                it.activityInfo.applicationInfo.packageName,
                it.activityInfo.name,
                it.loadLabel(packageManager).toString(),
                it.loadIcon(packageManager))
        }
    }

}