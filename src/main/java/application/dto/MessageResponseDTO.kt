package application.dto

import jdk.jfr.Percentage
import java.util.ArrayList

data class MessageResponseDTO(
    val timestamp : Long,

    val cpuCount : Long,
    val cpuLoadTotal : Long,
    val cpuLoad : Map<String, Long>,
    val cpuLastLoadPerMinute : Map<String, List<Long>>,

    val memTotal : Long,
    val memFree : Long,
    val memUsed : Long,
    val memCached : Long,
    val memFreePercentage : Long,

    val netTotalInBytes : Long,
    val netTotalOutBytes : Long,
    val netTotalInBytesPerSecond : Long,
    val netTotalOutBytesPerSecond : Long,
    val networkLastMinuteStatInPercentage : List<Long>,
    val networkLastMinuteStatOutPercentage : List<Long>,
    val netInterfaces : List<NetworkInterfaceDTO>,

    val disks : List<DiskDTO>
    )
