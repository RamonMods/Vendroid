package io.github.ramonmods.vendroid.utils.exception

import android.content.Context
import io.github.ramonmods.vendroid.R
import io.github.ramonmods.vendroid.utils.exception.base.FatalException
import kotlinx.parcelize.Parcelize

@Parcelize
class UnknownException(override val cause: Throwable) : FatalException("Unknown error", cause) {
    override fun getUiMessage(context: Context): String {
        return context.getString(R.string.an_unknown_error_occurred, cause.message)
    }
}