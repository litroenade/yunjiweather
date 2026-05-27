package com.litroenade.yunjiweather.data.repository

import com.litroenade.yunjiweather.data.api.model.QWeatherWarningResponse
import com.litroenade.yunjiweather.data.entity.WarningEntity
import com.litroenade.yunjiweather.utils.DateTimeUtils
import java.io.IOException

class QWeatherWarningMapper {

    @Throws(IOException::class)
    fun map(
        locationId: String,
        warnings: List<QWeatherWarningResponse.Warning?>?,
        warningStore: WarningStore
    ): List<WarningEntity> {
        if (warnings == null) {
            throw IOException("QWeather warning list missing")
        }
        val result = ArrayList<WarningEntity>()
        for (warning in warnings) {
            if (warning == null) {
                throw IOException("QWeather warning item missing")
            }
            val warningId = requireText(warning.id, "warning.id")
            val oldWarning = warningStore.findByWarningId(locationId, warningId)
            val typeText = if (hasText(warning.typeName)) {
                warning.typeName
            } else {
                requireText(warning.type, "warning.type")
            }
            val severityText = if (hasText(warning.severityColor)) {
                warning.severityColor
            } else {
                requireText(warning.severity, "warning.severity")
            }
            result.add(
                WarningEntity(
                    warningId,
                    locationId,
                    requireText(warning.title, "warning.title"),
                    typeText,
                    severityText,
                    requireText(warning.text, "warning.text"),
                    parsePublishTime(warning.pubTime),
                    oldWarning != null && oldWarning.isRead,
                    oldWarning != null && oldWarning.isNotified
                )
            )
        }
        return result
    }

    @Throws(IOException::class)
    private fun parsePublishTime(value: String?): Long {
        return DateTimeUtils.parseQWeatherTime(requireText(value, "warning.pubTime"))
    }

    private fun hasText(value: String?): Boolean {
        return value != null && value.trim().isNotEmpty()
    }

    @Throws(IOException::class)
    private fun requireText(value: String?, fieldName: String): String {
        if (!hasText(value)) {
            throw IOException("QWeather warning response missing field: $fieldName")
        }
        return value!!
    }
}
