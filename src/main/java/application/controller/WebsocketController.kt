package application.controller

import application.dto.DiskDTO
import application.dto.MessageResponseDTO
import application.service.CpuService
import application.service.NetworkService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow


@Controller
@EnableScheduling
open class WebsocketController {

    @Autowired
    lateinit var template: SimpMessagingTemplate

    @Autowired
    lateinit var networkService : NetworkService

    @Autowired
    lateinit var cpuService: CpuService

    fun round(size : Long, pow : Int) : Double {
        if (pow == 0) return size.toDouble()
        if (pow == 1) return Math.round(size.toDouble() / 1024.0 * 100.0) / 100.0
        return Math.round(size.toDouble() / 1024.0.pow(pow.toDouble()) * 100.0) / 100.0
    }

    fun toHumanReadable(size: Long) : String {
        if (size < 1024L) return "$size Bytes"
        if (size < 1024L * 1024L) return "${round(size, 1)} KB"
        if (size < 1024L * 1024L * 1024L) return "${round(size, 2)} MB"
        if (size < 1024L * 1024L * 1024L * 1024L) return "${round(size, 3)} GB"
        if (size < 1024L * 1024L * 1024L * 1024L * 1024L) return "${round(size, 4)} TB"
        return "$size Unknown"
    }

    @Scheduled(fixedRate = 1000)
    open fun greeting() {
        val disks = ArrayList<DiskDTO>()
        Files.readAllLines(File("/proc/mounts").toPath())
            .stream()
            .map { it.trim() }
            .map { it.replace("\\s{2,128}".toRegex(), " ").trim() }
            .map { it.split("\\s+".toRegex()) }
            .filter { it[0].contains("/") && !it[1].startsWith("/etc/") }
            .forEach {
                disks.add(DiskDTO(
                    it[0],
                    it[1],
                    toHumanReadable(File(it[1]).freeSpace),
                    toHumanReadable(File(it[1]).totalSpace),
                    toHumanReadable(File(it[1]).usableSpace)
                ))
            }

        val mem = HashMap<String, Long>()
        Files.readAllLines(File("/proc/meminfo").toPath())
            .stream()
            .map { it.trim() }
            .map { it.replace("\\s{2,128}".toRegex(), " ").trim() }
            .map { it.split("\\s+".toRegex()) }
            .forEach {
                mem[it[0].replace(":", "").trim()] = it[1].toLong()
            }

        val memTotal = mem["MemTotal"] ?: 0
        val memFree = mem["MemAvailable"] ?: 0
        val message = MessageResponseDTO(
            Date().time,
            Runtime.getRuntime().availableProcessors().toLong(),
            cpuService.cpuCurrentValue["cpu"] ?: 0,
            cpuService.cpuCurrentValue,
            cpuService.cpuLoadLastMinutePercentage,
            memTotal / 1024,
            memFree / 1024,
            (memTotal - memFree) / 1024,
            ((mem["Buffers"] ?: 0) + (mem["Cached"] ?: 0)) / 1024,
            100 - ((memFree.toDouble() / memTotal.toDouble()) * 100.0).toLong(),
            networkService.totalNetworkInUsage.get(),
            networkService.totalNetworkOutUsage.get(),
            networkService.totalNetworkInUsagePS.get(),
            networkService.totalNetworkOutUsagePS.get(),
            networkService.networkLastMinuteStatInPercentage,
            networkService.networkLastMinuteStatOutPercentage,
            networkService.networkInterfacesList,
            disks
        )

        template.convertAndSend("/topic/general", message)
    }
}