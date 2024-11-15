

import com.google.gson.*
import com.outsystems.plugins.healthfitness.OSHealthFitnessWarning
import com.outsystems.plugins.healthfitness.data.HealthEnumTimeUnit
import java.lang.reflect.Type

class TimeUnitSerializer(
     private val onDeprecatedFound: () -> Unit
) : JsonSerializer<HealthEnumTimeUnit>, JsonDeserializer<HealthEnumTimeUnit> {

    // Serialize: How to write Status to JSON
    override fun serialize(src: HealthEnumTimeUnit?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.name)  // Use the enum name as JSON value
    }

    // Deserialize: How to read Status from JSON
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): HealthEnumTimeUnit {
        return when (val value = json?.asString) {
            "SECONDS", "MILLISECONDS" -> {
                onDeprecatedFound()
                HealthEnumTimeUnit.MINUTE
            }
            "MINUTE" -> HealthEnumTimeUnit.MINUTE
            "HOUR" -> HealthEnumTimeUnit.HOUR
            "DAY" -> HealthEnumTimeUnit.DAY
            "MONTH" -> HealthEnumTimeUnit.MONTH
            "YEAR" -> HealthEnumTimeUnit.YEAR
            else -> throw JsonParseException("Unknown time unit: $value")
        }
    }
}