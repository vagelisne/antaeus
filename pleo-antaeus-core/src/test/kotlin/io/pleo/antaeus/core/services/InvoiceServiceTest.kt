package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import kotlin.random.Random

class InvoiceServiceTest {
    private val invoices: ArrayList<Invoice> = ArrayList()
    private val invoicesPending: ArrayList<Invoice> = ArrayList()
    private val customers: ArrayList<Customer> = ArrayList()

    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
        every { fetchPendingInvoices() } returns invoicesPending
        every { fetchInvoices() } returns invoices
    }

    private val invoiceService = InvoiceService(dal)

    @BeforeEach
    fun setupTestsInitialData() {
        (1..100).forEach {
            customers.add(
                Customer(
                    it,
                    Currency.values()[Random.nextInt(0, Currency.values().size)]
                )
            )
        }

        customers.forEach { customer ->
            (1..10).forEach {
                val money = Money(
                    value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                    currency = customer.currency
                )
                val status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID
                val invoice = Invoice(it, customer.id, money, status)
                invoices.add(invoice)
                if (invoice.status == InvoiceStatus.PENDING) {
                    invoicesPending.add(invoice)
                }
            }
        }
    }

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `Fetch all invoices`() {
        val allInvoices = invoiceService.fetchAll()
        Assertions.assertEquals(1000, allInvoices.size)
    }

    @Test
    fun `Fetch pending invoices`() {
        val pending = invoiceService.fetchPending()
        Assertions.assertEquals(
            100,
            pending.size
        ) // All customers have 10 invoices from which only the first is PENDING
    }
}
