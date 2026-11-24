package io.github.ramonmods.vendroid.utils.exception.base

abstract class FatalException(message: String, cause: Throwable? = null) :
    VendroidException(message, cause)