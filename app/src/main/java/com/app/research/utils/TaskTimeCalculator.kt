package com.app.research.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs


const val noWork = "NO WORK"
const val lunch = "Lunch"
const val internetIssue = "Internet Issue"

val tasks: List<Task> = listOf(
    Task(1007, "No  Work", "am"),
    Task(1036, "Mindful Monday", "am"),
    Task(1040, "BinBag Demo giving", "am"),
    Task(1206, "ForeGolf"),
    Task(1259, "Binbag Hardik"),
    Task(107, "ForeGolf"),
    Task(135, "Lunch", exclude = true),
    Task(152, "ForeGolf"),
    Task(352, "Vikas"),
    Task(345, "Kiran, Vikas, Surbhi"),
    Task(400, "ForeGolf"),
    Task(getCurrentTime().first, "TimeOver", amPM = getCurrentTime().second.lowercase()),
)


fun main() {
    calculateTaskTime(tasks).forEach { println(it) }
}


private fun convertTo24HourFormat(time: String): LocalTime {
    val inputFormatter =
        DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH) // Ensure proper locale
    return LocalTime.parse(time, inputFormatter)
}

data class Task(
    val time: Int, // 0105 PM
    val task: String,
    val amPM: String = "pm",
    val exclude: Boolean = false
)

fun getCurrentTime(): Pair<Int, String> {
    val currentTime = LocalTime.now()
    val format = currentTime.format(DateTimeFormatter.ofPattern("hhmma", Locale.ENGLISH))
    return format.dropLast(2).toInt() to format.takeLast(2)
}

data class TimeLine(
    val task: String,
    val duration: String
) {
    override fun toString(): String {
        return "$task: $duration"
    }
}

private fun calculateTimeInMinutes(time: Int, amPM: String): Int {
    val formattedTime = "%02d:%02d %s".format(
        time / 100,
        time % 100,
        amPM.uppercase()
    ) // Ensure AM/PM uppercase
    val localTime = convertTo24HourFormat(formattedTime)
    return localTime.hour * 60 + localTime.minute
}

fun calculateTaskTime(
    task: List<Task>
): List<TimeLine> {
    if (task.size < 2) throw IllegalArgumentException("Task list should have at least 2 tasks")

    val tempList = mutableListOf<Pair<String, Int>>()

    for (i in 0 until task.size - 1) {
        if (task[i].exclude) continue
        val currentTaskTimeInMinutes = calculateTimeInMinutes(task[i].time, task[i].amPM)
        val nextTaskTimeInMinutes = calculateTimeInMinutes(task[i + 1].time, task[i + 1].amPM)
        val duration = abs(currentTaskTimeInMinutes - nextTaskTimeInMinutes)
        tempList.add(task[i].task to duration)
    }
    tempList.add("Buffer" to 30)
    val totalHourTillNow = tempList.sumOf { it.second }
    tempList.add("Total Hour: " to totalHourTillNow)
    tempList.add("Total Hour Left: " to (8 * 60 + 45) - totalHourTillNow)
    tempList.add("ORG Hr: " to totalHourTillNow - 30)

    return tempList.groupBy { it.first }.map { (group, items) ->
        TimeLine(
            task = group,
            duration = "${items.sumOf { it.second } / 60}h ${items.sumOf { it.second } % 60}m"
        )
    }
}