package com.example

import com.example.data.local.SeedData
import com.example.data.model.TicketStatus
import com.example.util.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {

    @Test
    fun testSeedDataGeneratesValidTickets() {
        val seedTickets = SeedData.getInitialTickets()
        assertTrue("Seed data should contain tickets", seedTickets.isNotEmpty())

        val bachDangTickets = seedTickets.filter { it.fromStation.contains("Bạch Đằng") }
        assertTrue("Should have tickets from Bạch Đằng", bachDangTickets.isNotEmpty())

        val thienLyTickets = seedTickets.filter { it.fromStation.contains("Thiên Lý") }
        assertTrue("Should have tickets from Trường MN Thiên Lý", thienLyTickets.isNotEmpty())

        seedTickets.forEach { ticket ->
            assertTrue("Ticket code must start with SWB-", ticket.code.startsWith("SWB-"))
            assertEquals("Initial status must be PENDING", TicketStatus.PENDING.name, ticket.status)
            assertTrue("Category must be adult or child", ticket.passengerCategory == "Vé người lớn" || ticket.passengerCategory == "Vé trẻ em")
        }
    }

    @Test
    fun testQrCodeGeneration() {
        val testCode = "SWB-BDLD-20261112-A01"
        val bitmap = QrCodeGenerator.generateQrBitmap(testCode, 200, 200)
        assertNotNull("Generated QR bitmap should not be null", bitmap)
        assertEquals(200, bitmap?.width)
        assertEquals(200, bitmap?.height)
    }
}
