package com.tezov.gofo.misc

import com.tezov.lib_java.toolbox.Clock
import org.threeten.bp.ZoneId

object ClockFormat {
    fun longToDateTime_FULL(value: Long?, zoneId: ZoneId? = Clock.ZoneIdLocal()): String? {
        var data: String? = null
        if (value != null && zoneId != null) {
            data = Clock.MilliSecondTo.DateAndTime.toString(
                value,
                zoneId,
                Clock.FormatDateAndTime.FULL
            )
        }
        return data
    }
}