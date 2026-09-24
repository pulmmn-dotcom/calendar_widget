package com.pulmm.shiftcalendar.data

fun interface DataChangeListener {
    suspend fun onDataChanged()
}
