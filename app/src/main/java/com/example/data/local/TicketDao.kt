package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY seatNumber ASC")
    fun getAllTickets(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE tripCode = :tripCode ORDER BY seatNumber ASC")
    fun getTicketsForTrip(tripCode: String): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE code = :code LIMIT 1")
    suspend fun getTicketByCode(code: String): TicketEntity?

    @Query("SELECT * FROM tickets WHERE status = 'CHECKED_IN' ORDER BY checkedInAt DESC")
    fun getCheckedInTickets(): Flow<List<TicketEntity>>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripCode = :tripCode AND status = 'CHECKED_IN'")
    fun getCheckedInCount(tripCode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM tickets WHERE tripCode = :tripCode")
    fun getTotalTicketCount(tripCode: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickets(tickets: List<TicketEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity)

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("UPDATE tickets SET status = 'CHECKED_IN', checkedInAt = :timestamp, checkedInStation = :station, checkedInStaff = :staff WHERE code = :code")
    suspend fun checkInTicket(code: String, timestamp: Long, station: String, staff: String): Int

    @Query("UPDATE tickets SET status = 'PENDING', checkedInAt = NULL, checkedInStation = NULL, checkedInStaff = NULL WHERE code = :code")
    suspend fun revertCheckIn(code: String): Int

    @Query("UPDATE tickets SET status = 'PENDING', checkedInAt = NULL, checkedInStation = NULL, checkedInStaff = NULL")
    suspend fun resetAllStatus()

    @Query("SELECT COUNT(*) FROM tickets")
    suspend fun getCount(): Int
}
