package application.service

import application.dto.NetworkInterfaceDTO
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
@EnableScheduling
open class NetworkService {
    public val totalNetworkInUsage = AtomicLong(0)
    public val totalNetworkOutUsage = AtomicLong(0)
    public val totalNetworkInUsagePS = AtomicLong(0)
    public val totalNetworkOutUsagePS = AtomicLong(0)
    public val totalNetworkInUsageLast = AtomicLong(0)
    public val totalNetworkOutUsageLast = AtomicLong(0)
    public val networkInterfacesList = CopyOnWriteArrayList<NetworkInterfaceDTO>()
    public val networkInterfacesListLast = HashMap<String, NetworkInterfaceDTO>()
    public val networkLastMinuteStatIn = CopyOnWriteArrayList<Long>()
    public val networkLastMinuteStatOut = CopyOnWriteArrayList<Long>()
    public val networkLastMinuteStatInPercentage = CopyOnWriteArrayList<Long>()
    public val networkLastMinuteStatOutPercentage = CopyOnWriteArrayList<Long>()

    @Scheduled(fixedRate = 1000)
    open fun calcNetworkLoad() {
        totalNetworkInUsage.set(0)
        totalNetworkOutUsage.set(0)
        networkInterfacesList.clear()

        if (networkLastMinuteStatIn.isEmpty())
            for (i in 0 until  60) networkLastMinuteStatIn.add(0)
        if (networkLastMinuteStatOut.isEmpty())
            for (i in 0 until 60) networkLastMinuteStatOut.add(0)

        Files.readAllLines(File("/proc/net/dev").toPath())
            .stream()
            .filter { it.contains(":") }
            .map { it.replace("\\s{2,128}".toRegex(), " ").trim() }
            .map { it.split("\\s+".toRegex()) }
            .filter {
                !it[0].startsWith("veth") &&
                        !it[0].startsWith("vnet") &&
                        !it[0].startsWith("lo") &&
                        !it[0].startsWith("docker") &&
                        !it[0].startsWith("wlp")
            }
            .forEach {
                val name = it[0].replace(":", "").trim()
                val inBytes = it[1].toLong()
                val outBytes = it[9].toLong()

                totalNetworkInUsage.addAndGet(inBytes)
                totalNetworkOutUsage.addAndGet(outBytes)

                if (totalNetworkInUsageLast.get() > 0)
                    totalNetworkInUsagePS.set(totalNetworkInUsage.get() - totalNetworkInUsageLast.get())
                if (totalNetworkOutUsageLast.get() > 0)
                    totalNetworkOutUsagePS.set(totalNetworkOutUsage.get() - totalNetworkOutUsageLast.get())

                networkInterfacesList.add(NetworkInterfaceDTO(
                    inBytes,
                    outBytes,
                    inBytes - (networkInterfacesListLast[name]?.netInBytes ?: inBytes),
                    outBytes - (networkInterfacesListLast[name]?.netOutBytes ?: outBytes),
                    name,
                    "0.0.0.0", // TODO: get ip
                    (Files.readString(File("/sys/class/net/$name/address").toPath()) ?: "00:00:00:00:00:00").uppercase(
                        Locale.getDefault())))
            }

        for (i in 1 until 60) {
            if (i < networkLastMinuteStatIn.size)
                networkLastMinuteStatIn[i - 1] = networkLastMinuteStatIn[i]
            else
                networkLastMinuteStatIn.add(0);
            if (i < networkLastMinuteStatOut.size)
                networkLastMinuteStatOut[i - 1] = networkLastMinuteStatOut[i]
            else
                networkLastMinuteStatOut.add(0);
        }
        networkLastMinuteStatIn[networkLastMinuteStatIn.size - 1] = totalNetworkInUsagePS.get()
        networkLastMinuteStatOut[networkLastMinuteStatOut.size - 1] = totalNetworkOutUsagePS.get()
        val minuteMaxIn = networkLastMinuteStatIn.stream().max { o1, o2 -> o1.compareTo(o2) }.get()
        val minuteMaxOut = networkLastMinuteStatOut.stream().max { o1, o2 -> o1.compareTo(o2) }.get()

        networkLastMinuteStatInPercentage.clear()
        networkLastMinuteStatInPercentage.addAll(
            networkLastMinuteStatIn.map { e -> ((e.toDouble() / minuteMaxIn.toDouble()) * 100.0).toLong() }
        )
        networkLastMinuteStatOutPercentage.clear()
        networkLastMinuteStatOutPercentage.addAll(
            networkLastMinuteStatOut.map { e -> ((e.toDouble() / minuteMaxOut.toDouble()) * 100.0).toLong() }
        )

        networkInterfacesListLast.clear()
        networkInterfacesList.forEach { networkInterfacesListLast[it.ifName] = it }
        totalNetworkInUsageLast.set(totalNetworkInUsage.get())
        totalNetworkOutUsageLast.set(totalNetworkOutUsage.get())
    }
}