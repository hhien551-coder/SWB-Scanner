package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TicketStatus {
    PENDING,
    CHECKED_IN,
    CANCELLED
}

enum class TicketType {
    STANDARD,      // Vé phổ thông (15.000 VNĐ)
    ROUND_TRIP,    // Vé khứ hồi (30.000 VNĐ)
    VIP_DECK,      // Vé khoang ngắm cảnh VIP
    DISCOUNT       // Vé ưu tiên
}

object PassengerCategory {
    const val ADULT = "Vé người lớn"
    const val CHILD = "Vé trẻ em"
}

enum class ValidationStatus {
    VALID,              // Vé hợp lệ, mời lên tàu
    ALREADY_CHECKED_IN, // Vé đã soát trước đó
    WRONG_TRIP,         // Vé thuộc chuyến khác hoặc ngày khác
    WRONG_STATION,      // Vé không xuất phát từ ga này
    NOT_FOUND,          // Mã vé không tồn tại trong hệ thống
    INVALID_FORMAT      // Mã QR không đúng định dạng SWB
}

@Entity(tableName = "tickets")
data class TicketEntity(
    @PrimaryKey
    val code: String,              // Mã QR vé: e.g. "SWB-BDLD-20261112-A01"
    val passengerCategory: String, // "Vé người lớn" hoặc "Vé trẻ em" (không hiển thị tên người)
    val phone: String = "",        // Ghi chú phụ
    val tripCode: String,          // Mã chuyến: e.g. "SWB-01"
    val boatName: String,          // Tên tàu: e.g. "Saigon Waterbus 01"
    val fromStation: String,       // Bến đi
    val toStation: String,         // Bến đến
    val departureTime: String,     // Giờ khởi hành: e.g. "08:30"
    val seatNumber: String,        // Số ghế: e.g. "A-01"
    val ticketType: String,        // Loại vé: STANDARD, VIP_DECK, DISCOUNT,...
    val price: Long,               // Giá vé (VNĐ)
    val status: String,            // PENDING, CHECKED_IN, CANCELLED
    val checkedInAt: Long? = null, // Thời gian quét (epoch ms)
    val checkedInStation: String? = null, // Bến quét vé
    val checkedInStaff: String? = null    // Nhân viên soát vé
)

data class ScanEvent(
    val ticket: TicketEntity?,
    val status: ValidationStatus,
    val rawCode: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TripInfo(
    val tripCode: String,
    val boatName: String,
    val route: String,
    val departureTime: String,
    val totalCapacity: Int = 75
)

object SwbStations {
    const val BACH_DANG = "Bến Bạch Đằng"
    const val BINH_AN = "Bến Bình An"
    const val THANH_DA = "Bến Thanh Đa"
    const val HIEP_BINH_CHANH = "Bến Hiệp Bình Chánh"
    const val LINH_DONG = "Bến Linh Đông"
    const val THIEN_LY = "Bến Trường MN Thiên Lý"

    val ALL_STATIONS = listOf(
        BACH_DANG,
        BINH_AN,
        THANH_DA,
        HIEP_BINH_CHANH,
        LINH_DONG,
        THIEN_LY
    )
}

object SwbTrips {
    val TRIPS = listOf(
        TripInfo("SWB-01", "Tàu SWB Catamaran 01", "Bạch Đằng ➔ Linh Đông", "08:30", 75),
        TripInfo("SWB-02", "Tàu SWB Catamaran 02", "Linh Đông ➔ Bạch Đằng", "09:15", 75),
        TripInfo("SWB-03", "Tàu SWB Catamaran 03", "Bạch Đằng ➔ Linh Đông", "10:00", 75),
        TripInfo("SWB-04", "Tàu SWB Catamaran 04", "Bạch Đằng ➔ Thanh Đa", "14:30", 75),
        TripInfo("SWB-05", "Tàu SWB Sunset VIP", "Bạch Đằng ➔ Linh Đông (Hoàng Hôn)", "17:15", 75),
        TripInfo("SWB-06", "Tàu SWB Night Cruise", "Bạch Đằng ➔ Bình An (Đêm)", "19:00", 75)
    )
}
