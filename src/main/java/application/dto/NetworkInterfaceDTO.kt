package application.dto

data class NetworkInterfaceDTO(
    val netInBytes : Long,
    val netOutBytes : Long,
    val netInBytesPerSecond : Long,
    val netOutBytesPerSecond : Long,
    val ifName : String,
    val ifIpAddress : String,
    val ifMacAddress : String
    )
