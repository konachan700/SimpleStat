package application.dto

data class DiskDTO(
    val dev : String,
    val mountPoint : String,
    val free : String,
    val total : String,
    val used : String
)
