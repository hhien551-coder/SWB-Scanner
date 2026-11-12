package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.PassengerCategory
import com.example.data.model.ScanEvent
import com.example.data.model.SwbStations
import com.example.data.model.SwbTrips
import com.example.data.model.TicketEntity
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType
import com.example.data.model.TripInfo
import com.example.data.model.ValidationStatus
import com.example.data.repository.TicketRepository
import com.example.ui.theme.SwbErrorRed
import com.example.ui.theme.SwbValidGreen
import com.example.util.FeedbackManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TicketRepository
    private val feedbackManager: FeedbackManager

    val allTickets: StateFlow<List<TicketEntity>>
    val checkedInCount: StateFlow<Int>
    val totalCount: StateFlow<Int>

    private val _currentStation = MutableStateFlow(SwbStations.BACH_DANG)
    val currentStation: StateFlow<String> = _currentStation.asStateFlow()

    private val _currentTrip = MutableStateFlow(SwbTrips.TRIPS[0])
    val currentTrip: StateFlow<TripInfo> = _currentTrip.asStateFlow()

    private val _isContinuousMode = MutableStateFlow(true)
    val isContinuousMode: StateFlow<Boolean> = _isContinuousMode.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _zoomRatio = MutableStateFlow(0f) // 0 = 1x, 0.25 = 1.5x, 0.5 = 2x
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private val _lastScanEvent = MutableStateFlow<ScanEvent?>(null)
    val lastScanEvent: StateFlow<ScanEvent?> = _lastScanEvent.asStateFlow()

    private val _isFlashingBorder = MutableStateFlow(false)
    val isFlashingBorder: StateFlow<Boolean> = _isFlashingBorder.asStateFlow()

    private val _flashColor = MutableStateFlow(SwbValidGreen)
    val flashColor: StateFlow<Color> = _flashColor.asStateFlow()

    private val _scanHistory = MutableStateFlow<List<ScanEvent>>(emptyList())
    val scanHistory: StateFlow<List<ScanEvent>> = _scanHistory.asStateFlow()

    private val _showResultCard = MutableStateFlow(false)
    val showResultCard: StateFlow<Boolean> = _showResultCard.asStateFlow()

    private val _staffGate = MutableStateFlow("Cổng soát vé 01")
    val staffGate: StateFlow<String> = _staffGate.asStateFlow()

    private val _strictTripCheck = MutableStateFlow(false)
    val strictTripCheck: StateFlow<Boolean> = _strictTripCheck.asStateFlow()

    // Debounce tracker for continuous scanning
    private val recentlyScanned = mutableMapOf<String, Long>()
    private var dismissJob: Job? = null
    private var flashJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = TicketRepository(db.ticketDao())
        feedbackManager = FeedbackManager(application)

        allTickets = repository.allTickets.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        checkedInCount = repository.getCheckedInCount("SWB-01").stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

        totalCount = repository.getTotalCount("SWB-01").stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            25
        )
    }

    fun onQrScanned(rawCode: String) {
        val now = System.currentTimeMillis()
        val lastSeen = recentlyScanned[rawCode]

        // Cooldown of 3 seconds (3000ms) for consecutive scans of the same code
        if (lastSeen != null && now - lastSeen < 3000L) {
            return
        }
        recentlyScanned[rawCode] = now

        viewModelScope.launch {
            val event = repository.validateAndCheckIn(
                rawScannedString = rawCode,
                currentStation = _currentStation.value,
                currentTrip = _currentTrip.value,
                staffName = _staffGate.value,
                strictTripCheck = _strictTripCheck.value
            )

            _lastScanEvent.value = event
            _scanHistory.value = listOf(event) + _scanHistory.value.take(49)
            _showResultCard.value = true

            // Trigger multisensory feedback
            val isSuccess = event.status == ValidationStatus.VALID
            _flashColor.value = if (isSuccess) SwbValidGreen else SwbErrorRed
            triggerFlashBorder()

            feedbackManager.onScanResult(event.status, event.message)

            // Auto dismiss card after 3 seconds while showing success result
            dismissJob?.cancel()
            dismissJob = launch {
                delay(3000L)
                _showResultCard.value = false
            }
        }
    }

    private fun triggerFlashBorder() {
        flashJob?.cancel()
        flashJob = viewModelScope.launch {
            _isFlashingBorder.value = true
            delay(550)
            _isFlashingBorder.value = false
        }
    }

    fun simulateScan(ticketCode: String) {
        onQrScanned(ticketCode)
    }

    fun dismissResultCard() {
        dismissJob?.cancel()
        _showResultCard.value = false
    }

    fun toggleContinuousMode() {
        _isContinuousMode.value = !_isContinuousMode.value
    }

    fun toggleTorch() {
        _isTorchOn.value = !_isTorchOn.value
    }

    fun setZoom(ratio: Float) {
        _zoomRatio.value = ratio
    }

    fun setStation(station: String) {
        _currentStation.value = station
    }

    fun setTrip(trip: TripInfo) {
        _currentTrip.value = trip
    }

    fun toggleStrictTripCheck() {
        _strictTripCheck.value = !_strictTripCheck.value
    }

    fun revertCheckIn(code: String) {
        viewModelScope.launch {
            repository.revertCheckIn(code)
        }
    }

    fun resetAllBoarding() {
        viewModelScope.launch {
            repository.resetAll()
            _scanHistory.value = emptyList()
            recentlyScanned.clear()
            _lastScanEvent.value = null
            _showResultCard.value = false
        }
    }

    fun createAndAddTicket(
        passengerCategory: String,
        seat: String,
        fromStation: String,
        toStation: String,
        type: TicketType = TicketType.STANDARD
    ): String {
        val safeSeat = seat.ifBlank { "A-${(10..99).random()}" }
        val code = "SWB-QR-${System.currentTimeMillis() % 1000000}-$safeSeat"
        val price = when {
            passengerCategory == PassengerCategory.CHILD -> 10000L
            type == TicketType.VIP_DECK -> 25000L
            type == TicketType.ROUND_TRIP -> 30000L
            else -> 15000L
        }

        val newTicket = TicketEntity(
            code = code,
            passengerCategory = passengerCategory,
            phone = "",
            tripCode = _currentTrip.value.tripCode,
            boatName = _currentTrip.value.boatName,
            fromStation = fromStation,
            toStation = toStation,
            departureTime = _currentTrip.value.departureTime,
            seatNumber = safeSeat,
            ticketType = type.name,
            price = price,
            status = TicketStatus.PENDING.name
        )

        viewModelScope.launch {
            repository.addNewTicket(newTicket)
        }

        return code
    }

    override fun onCleared() {
        super.onCleared()
        feedbackManager.release()
    }
}
