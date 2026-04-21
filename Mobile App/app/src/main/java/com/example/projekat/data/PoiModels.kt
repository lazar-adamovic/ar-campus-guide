@file:Suppress("SpellCheckingInspection")

package com.example.projekat.data
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.projekat.R
import com.google.gson.annotations.SerializedName

data class PoiDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val categoryId: Int,
    val description: String,
    val websiteUrl: String?,
    val categoryName: String,
    val modelFileName: String,
    val iconName: String
)

data class CategoryDto(
    val id: Int,
    val name: String,
    val modelFileName: String,
    val iconName: String
)

data class VerifyAdminCommand(val password: String)
data class AdminResponse(val success: Boolean)
fun getIconResId(iconName: String?): Int {
    return when (iconName) {
        "ic_fakultet" -> R.drawable.ic_fakultet
        "ic_kafic" -> R.drawable.ic_kafic
        "ic_sluzba" -> R.drawable.ic_sluzba
        "ic_dom" -> R.drawable.ic_dom
        "ic_menza" -> R.drawable.ic_menza
        "ic_znamenitost" -> R.drawable.ic_znamenitost
        else -> org.osmdroid.library.R.drawable.marker_default
    }
}
class SimplePasswordTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val maskedText = "*".repeat(text.text.length)
        return TransformedText(AnnotatedString(maskedText), OffsetMapping.Identity)
    }
}
data class UpdateDescriptionDto(
    @SerializedName("Content")
    val content: String
)