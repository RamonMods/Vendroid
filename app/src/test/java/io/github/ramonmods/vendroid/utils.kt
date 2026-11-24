package io.github.ramonmods.vendroid

import io.github.ramonmods.vendroid.plugins.telemetry.Telemetry

fun setUpMockTelemetry() {
    Telemetry.TESTS_ONLY_setTestMode(true)
}
