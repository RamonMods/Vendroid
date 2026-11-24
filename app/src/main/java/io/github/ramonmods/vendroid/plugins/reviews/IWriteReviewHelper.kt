package io.github.ramonmods.vendroid.plugins.reviews

interface IWriteReviewHelper {
    val isGPlayFlavor: Boolean
    fun launchReviewFlow()
}