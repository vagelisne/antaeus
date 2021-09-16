package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.BusinessRuleException
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService
) {
    // How often the task should be run. With current setting it runs everyday
    private val period: Long = 24 * 60 * 60 * 1000;

    init {
        val startDate = convertToDate(LocalDateTime.now())
        // When the service is initialized this scheduler will call the charge method everyday
        Timer().schedule(startDate, period) { chargeCustomers() }
    }

    private fun convertToDate(dateToConvert: LocalDateTime): Date {
        return Timestamp.valueOf(dateToConvert)
    }

    fun chargeCustomers(
        userInitiated: Boolean = false,
        currentDate: LocalDate = LocalDate.now()
    ): List<Int>? {
        if (currentDate.dayOfMonth == 1) {
            invoiceService.fetchPending().forEach {
                val paid = payBill(it)
                if (paid) {
                    invoiceService.changeStatusToPaid(it.id)
                }
            }
            // Return the IDs of any unpaid invoices
            return invoiceService.fetchPending().map { it.id }
        } else if (userInitiated) {
            // If the user tries to charge the customers on any day other than the first of the month
            // throw an Exception
            throw BusinessRuleException("Cannot charge customers")
        }
        // When the function is called by the scheduler on any other day of the month return null
        return null
    }

    // Returns true is the bill is paid, otherwise false
    private fun payBill(invoice: Invoice): Boolean {
        return try {
            paymentProvider.charge(invoice)
        } catch (e: CurrencyMismatchException) {
            // If there is a currency mismatch change the invoice currency and try again
            val customer = customerService.fetch(invoice.customerId);
            if (invoice.amount.currency != customer.currency) {
                invoiceService.changeAmount(invoice.id, invoice.amount.convertTo(customer.currency))
            }
            paymentProvider.charge(invoiceService.fetch(invoice.id))
        } catch (e: CustomerNotFoundException) {
            logger.error(e.message)
            false
        } catch (e: NetworkException) {
            logger.error(e.message)
            false
        }
    }
}
