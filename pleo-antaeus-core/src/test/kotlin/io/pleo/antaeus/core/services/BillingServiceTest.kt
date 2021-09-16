package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class BillingServiceTest {
    private val invoice = Invoice(1, 1, Money(BigDecimal(0), Currency.EUR), InvoiceStatus.PENDING)
    private val customer = Customer(1, Currency.EUR)

    private val invoiceService = mockk<InvoiceService> {
        every { fetchPending() } returns listOf(invoice)
        every { fetch(1) } returns invoice
        every { changeStatusToPaid(1) } returns Unit
    }

    private val paymentProvider = mockk<PaymentProvider> {
        every { charge(invoice) } returns true
    }

    private val customerService = mockk<CustomerService> {
        every { fetch(1) } returns customer
    }

    private val billingService = BillingService(paymentProvider, invoiceService, customerService)

    @Test
    fun `test billingService`() {
        billingService.chargeCustomers(currentDate = LocalDate.now().withDayOfMonth(1));
        verify(exactly = 1) { paymentProvider.charge(invoice) }
    }
}