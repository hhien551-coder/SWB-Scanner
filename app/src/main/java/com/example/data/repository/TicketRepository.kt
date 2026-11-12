package com.example.data.repository

import com.example.data.local.TicketDao
import com.example.data.model.ScanEvent
import com.example.data.model.TicketEntity
import com.example.data.model.TicketStatus
import com.example.data.model.TripInfo
import com.example.data.model.ValidationStatus
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketRepository(private val ticketDao: TicketDao) {

    val allTickets: Flow<List<TicketEntity>> = ticketDao.getAllTickets()

    fun getTicketsForTrip(tripCode: String): Flow<List<TicketEntity>> =
        ticketDao.getTicketsForTrip(tripCode)

    fun getCheckedInCount(tripCode: String): Flow<Int> =
        ticketDao.getCheckedInCount(tripCode)

    fun getTotalCount(tripCode: String): Flow<Int> =
        ticketDao.getTotalTicketCount(tripCode)

    suspend fun validateAndCheckIn(
        rawScannedString: String,
        currentStation: String,
        currentTrip: TripInfo?,
        staffName: String = "NV Soát vé Cổng 1",
        strictTripCheck: Boolean = false
    ): ScanEvent {
        val extractedCode = extractTicketCode(rawScannedString.trim())
        if (extractedCode.isBlank()) {
            return ScanEvent(
                ticket = null,
                status = ValidationStatus.INVALID_FORMAT,
                rawCode = rawScannedString,
                message = "Mã QR không đúng định dạng Saigon Waterbus"
            )
        }

        val existingTicket = ticketDao.getTicketByCode(extractedCode)
        if (existingTicket == null) {
            // Check if it follows standard SWB format pattern (e.g. starts with SWB-)
            if (extractedCode.startsWith("SWB-", ignoreCase = true)) {
                // Dynamically create and accept valid ticket on the fly
                val newTicket = TicketEntity(
                    code = extractedCode,
                    passengerCategory = com.example.data.model.PassengerCategory.ADULT,
                    phone = "",
                    tripCode = currentTrip?.tripCode ?: "SWB-01",
                    boatName = currentTrip?.boatName ?: "Saigon Waterbus",
                    fromStation = currentStation,
                    toStation = "Bến Linh Đông",
                    departureTime = currentTrip?.departureTime ?: "08:30",
                    seatNumber = "Tự do",
                    ticketType = "STANDARD",
                    price = 15000L,
                    status = TicketStatus.CHECKED_IN.name,
                    checkedInAt = System.currentTimeMillis(),
                    checkedInStation = currentStation,
                    checkedInStaff = staffName
                )
                ticketDao.insertTicket(newTicket)
                return ScanEvent(
                    ticket = newTicket,
                    status = ValidationStatus.VALID,
                    rawCode = extractedCode,
                    message = "Vé điện tử hợp lệ - Xin mời lên tàu!"
                )
            }

            return ScanEvent(
                ticket = null,
                status = ValidationStatus.NOT_FOUND,
                rawCode = extractedCode,
                message = "Không tìm thấy thông tin vé trong hệ thống"
            )
        }



        // Strict Trip check if enabled
        if (strictTripCheck && currentTrip != null && existingTicket.tripCode != currentTrip.tripCode) {
            return ScanEvent(
                ticket = existingTicket,
                status = ValidationStatus.WRONG_TRIP,
                rawCode = extractedCode,
                message = "Vé chuyến ${existingTicket.tripCode} (${existingTicket.departureTime}), không phải chuyến hiện tại ${currentTrip.tripCode}"
            )
        }

        // Valid ticket: Check in now!
        val now = System.currentTimeMillis()
        ticketDao.checkInTicket(
            code = existingTicket.code,
            timestamp = now,
            station = currentStation,
            staff = staffName
        )

        val updatedTicket = existingTicket.copy(
            status = TicketStatus.CHECKED_IN.name,
            checkedInAt = now,
            checkedInStation = currentStation,
            checkedInStaff = staffName
        )

        return ScanEvent(
            ticket = updatedTicket,
            status = ValidationStatus.VALID,
            rawCode = extractedCode,
            message = "Vé hợp lệ - Xin mời lên tàu!"
        )
    }

    suspend fun revertCheckIn(code: String) {
        ticketDao.revertCheckIn(code)
    }

    suspend fun addNewTicket(ticket: TicketEntity) {
        ticketDao.insertTicket(ticket)
    }

    suspend fun resetAll() {
        ticketDao.resetAllStatus()
    }

    private fun extractTicketCode(raw: String): String {
        // e.g. "https://saigonwaterbus.vn/ticket/SWB-BDLD-20261112-A01"
        // or {"code":"SWB-123"}
        // or raw "SWB-BDLD-20261112-A01"
        if (raw.contains("/ticket/")) {
            return raw.substringAfter("/ticket/").substringBefore("?").trim()
        }
        if (raw.contains("/v/")) {
            return raw.substringAfter("/v/").substringBefore("?").trim()
        }
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            val lastSegment = raw.substringAfterLast("/")
            if (lastSegment.isNotBlank()) return lastSegment.substringBefore("?")
        }
        // Match JSON key "code"
        val codeMatch = Regex("\"code\"\\s*:\\s*\"([^\"]+)\"").find(raw)
        if (codeMatch != null) {
            return codeMatch.groupValues[1]
        }
        return raw.trim()
    }
}
