package io.github.ramonmods.vendroid.plugins.reviews

import android.app.Activity
import io.github.ramonmods.vendroid.utils.ProjectLinks

class WriteReviewHelper(private val mActivity: Activity) : IWriteReviewHelper {
    override val isGPlayFlavor: Boolean
        get() = false

    override fun launchReviewFlow() {
        ProjectLinks.open(mActivity, "https://github.com/RamonMods/Vendroid/")
    }
}
