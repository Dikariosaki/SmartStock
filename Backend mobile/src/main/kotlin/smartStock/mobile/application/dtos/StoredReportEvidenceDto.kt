package smartStock.mobile.application.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class StoredReportEvidenceDto(
    @JsonProperty("blobKeys") val blobKeys: List<String> = emptyList(),
    @JsonProperty("imageUrls") val legacyImageUrls: List<String> = emptyList(),
    @JsonProperty("observation") val observation: String? = null,
) {
    fun allImageReferences(): List<String> =
        (blobKeys + legacyImageUrls)
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
}
