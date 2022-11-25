package ca.georgiancollege.comp3025_f2022_week11

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TVShow(
    val id: String? = "",
    val title: String? = "",
    val studio: String? = ""
){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "studio" to studio
        )
    }
}
