package io.github.ramonmods.vendroid.utils.exception

import android.content.Context
import io.github.ramonmods.vendroid.R
import io.github.ramonmods.vendroid.utils.exception.base.FatalException
import kotlinx.parcelize.Parcelize

@Parcelize
class ServiceTimeoutException :
    FatalException("Timed out while waiting for service to start") {
    override fun getUiMessage(context: Context): String {
        return context.getString(R.string.service_timeout_message)
    }
}