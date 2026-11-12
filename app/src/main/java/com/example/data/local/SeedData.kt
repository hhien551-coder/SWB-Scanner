package com.example.data.local

import com.example.data.model.PassengerCategory
import com.example.data.model.SwbStations
import com.example.data.model.TicketEntity
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType

object SeedData {
    fun getInitialTickets(): List<TicketEntity> {
        val list = mutableListOf<TicketEntity>()

        // 1. Chuyến SWB-01: Bạch Đằng -> Linh Đông (08:30)
        val seatsSWB01 = (1..25).map { String.format("A-%02d", it) }

        seatsSWB01.forEachIndexed { index, seat ->
            val code = "SWB-BDLD-20261112-$seat"
            val isChild = (index == 6 || index == 12 || index == 18) // Mẫu vé trẻ em
            val category = if (isChild) PassengerCategory.CHILD else PassengerCategory.ADULT
            val type = when {
                index < 4 -> TicketType.VIP_DECK.name
                index % 4 == 0 -> TicketType.ROUND_TRIP.name
                isChild -> TicketType.DISCOUNT.name
                else -> TicketType.STANDARD.name
            }
            val price = when {
                isChild -> 10000L
                type == TicketType.VIP_DECK.name -> 25000L
                type == TicketType.ROUND_TRIP.name -> 30000L
                else -> 15000L
            }
            list.add(
                TicketEntity(
                    code = code,
                    passengerCategory = category,
                    phone = "",
                    tripCode = "SWB-01",
                    boatName = "Tàu SWB Catamaran 01",
                    fromStation = SwbStations.BACH_DANG,
                    toStation = SwbStations.LINH_DONG,
                    departureTime = "08:30",
                    seatNumber = seat,
                    ticketType = type,
                    price = price,
                    status = TicketStatus.PENDING.name
                )
            )
        }

        // 2. Chuyến tại Bến Bình An (SWB-02)
        val seatsSWB02 = (1..8).map { String.format("B-%02d", it) }
        seatsSWB02.forEachIndexed { index, seat ->
            val isChild = (index == 3 || index == 7)
            list.add(
                TicketEntity(
                    code = "SWB-BINHAN-20261112-$seat",
                    passengerCategory = if (isChild) PassengerCategory.CHILD else PassengerCategory.ADULT,
                    phone = "",
                    tripCode = "SWB-02",
                    boatName = "Tàu SWB Catamaran 02",
                    fromStation = SwbStations.BINH_AN,
                    toStation = SwbStations.BACH_DANG,
                    departureTime = "09:15",
                    seatNumber = seat,
                    ticketType = if (isChild) TicketType.DISCOUNT.name else TicketType.STANDARD.name,
                    price = if (isChild) 10000L else 15000L,
                    status = TicketStatus.PENDING.name
                )
            )
        }

        // 3. Vé bến Trường Mầm Non Thiên Lý (Đa số là Vé trẻ em & cô giáo)
        val seatsThienLy = (1..6).map { String.format("C-%02d", it) }
        seatsThienLy.forEachIndexed { index, seat ->
            val isChild = index >= 1 // C-02 đến C-06 là Vé trẻ em
            list.add(
                TicketEntity(
                    code = "SWB-THIENLY-20261112-$seat",
                    passengerCategory = if (isChild) PassengerCategory.CHILD else PassengerCategory.ADULT,
                    phone = "",
                    tripCode = "SWB-01",
                    boatName = "Tàu SWB Catamaran 01",
                    fromStation = SwbStations.THIEN_LY,
                    toStation = SwbStations.BACH_DANG,
                    departureTime = "08:30",
                    seatNumber = seat,
                    ticketType = if (isChild) TicketType.DISCOUNT.name else TicketType.STANDARD.name,
                    price = if (isChild) 10000L else 15000L,
                    status = TicketStatus.PENDING.name
                )
            )
        }

        // 4. Vé bến Hiệp Bình Chánh & Linh Đông
        val seatsHBC = (1..4).map { String.format("D-%02d", it) }
        seatsHBC.forEachIndexed { index, seat ->
            val isChild = index == 2
            list.add(
                TicketEntity(
                    code = "SWB-HBC-20261112-$seat",
                    passengerCategory = if (isChild) PassengerCategory.CHILD else PassengerCategory.ADULT,
                    phone = "",
                    tripCode = "SWB-03",
                    boatName = "Tàu SWB Catamaran 03",
                    fromStation = SwbStations.HIEP_BINH_CHANH,
                    toStation = SwbStations.BACH_DANG,
                    departureTime = "10:00",
                    seatNumber = seat,
                    ticketType = TicketType.STANDARD.name,
                    price = 15000L,
                    status = TicketStatus.PENDING.name
                )
            )
        }

        return list
    }
}
