package application.service

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Service
@EnableScheduling
open class CpuService {
    public val cpuCurrentValue = ConcurrentHashMap<String, Long>()
    public val cpuLastValues = ConcurrentHashMap<String, CopyOnWriteArrayList<Long>>()
    public val cpuLoadLastMinutePercentage = ConcurrentHashMap<String, CopyOnWriteArrayList<Long>>()

    @Scheduled(fixedRate = 1000)
    open fun calcCpuLoad() {
        Files.readAllLines(File("/proc/stat").toPath())
            .stream()
            .map { it.trim() }
            .filter { it.startsWith("cpu") }
            .map { it.replace("\\s{2,128}".toRegex(), " ").trim() }
            .map { it.split("\\s+".toRegex()) }
            .filter { it[0].length > 1 }
            .forEach {
                val cpuName = it[0]
                val values = it.subList(1, it.size).map { e -> e.toLong() }
                val lastValues = cpuLastValues[cpuName] ?: Collections.emptyList()
                if (lastValues.isNotEmpty()) {
                    cpuCurrentValue[cpuName] =
                        (((values[0] + values[1] + values[2]) - (lastValues[0] + lastValues[1] + lastValues[2])).toDouble() /
                                ((values[0] + values[1] + values[2] + values[3]) - (lastValues[0] + lastValues[1] + lastValues[2] + lastValues[3])).toDouble() * 100.0).toLong()
                }
                val list = CopyOnWriteArrayList<Long>()
                for (i in 0 .. 3) {
                    list.add(values[i])
                }
                if (cpuLoadLastMinutePercentage[cpuName] == null) {
                    cpuLoadLastMinutePercentage[cpuName] = CopyOnWriteArrayList<Long>()
                    for (i in 0..59) (cpuLoadLastMinutePercentage[cpuName]!! as CopyOnWriteArrayList).add(0)
                } else {
                    for (i in 1..59) cpuLoadLastMinutePercentage[cpuName]!![i - 1] = cpuLoadLastMinutePercentage[cpuName]!![i]
                    cpuLoadLastMinutePercentage[cpuName]!![cpuLoadLastMinutePercentage[cpuName]!!.size - 1] = cpuCurrentValue[cpuName] ?: 0
                }
                cpuLastValues[cpuName] = list;
            }
    }
}