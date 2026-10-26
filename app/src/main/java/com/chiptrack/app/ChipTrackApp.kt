package com.chiptrack.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.chiptrack.app.ui.AppFonts

class ChipTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppFonts.init(this)
        registerActivityLifecycleCallbacks(FontLifecycleCallbacks())
    }

    private class FontLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentViewCreated(
                            fm: FragmentManager,
                            f: Fragment,
                            v: android.view.View,
                            savedInstanceState: Bundle?
                        ) {
                            AppFonts.applyTree(v)
                        }
                    },
                    true
                )
            }
        }

        override fun onActivityStarted(activity: Activity) {
            val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
            AppFonts.applyTree(content)
            if (content.getTag(R.id.tag_app_font_watch) == true) return
            content.setTag(R.id.tag_app_font_watch, true)
            content.viewTreeObserver.addOnGlobalLayoutListener {
                AppFonts.applyTree(content)
            }
        }

        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
}
