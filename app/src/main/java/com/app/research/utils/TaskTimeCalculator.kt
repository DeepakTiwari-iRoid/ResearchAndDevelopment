package com.app.research.utils

import com.app.research.utils.TimePart.AM
import com.app.research.utils.TimePart.PM
import kotlin.math.abs


fun main() {
    calculateTaskTime(task).forEach { println(it) }
}

val task = listOf(
    Task("Edit Goal", 10, 58, AM),
    Task("Delete Goal", 11, 20, AM),
)

fun calculateTaskTime(
    task: List<Task>
): List<TimeLine> {
    if (task.size < 2) throw IllegalArgumentException("Task list should have at least 2 tasks")

    val tempList = mutableListOf<Pair<String, Int>>()

    for (i in 0 until task.size - 1) {
        val duration = abs(task[i].timeInMins - task[i + 1].timeInMins)
        tempList.add(Pair(task[i].task, duration))
    }

    return tempList.groupBy { it.first }.map { (task, durations) ->
        TimeLine(
            task = task,
            duration = "${durations.sumOf { it.second } / 60}h ${durations.sumOf { it.second } % 60}m")
    }
}


data class Task(
    val task: String,
    val hour: Int,
    val minute: Int,
    val amPM: TimePart = PM
) {
    val timeInMins
        get() = when (amPM) {
            AM -> (hour * 60) + minute
            PM -> (12 * 60) + (hour * 60) + minute
        }
}

data class TimeLine(
    val task: String,
    val duration: String
) {
    override fun toString(): String {
        return "$task: $duration"
    }
}

enum class TimePart { AM, PM }