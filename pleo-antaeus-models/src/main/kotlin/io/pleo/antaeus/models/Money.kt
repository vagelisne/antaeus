package io.pleo.antaeus.models

import java.math.BigDecimal
import kotlin.random.Random

data class Money(
    val value: BigDecimal,
    val currency: Currency
) {
    // Converts from one currency to another
    fun convertTo(currency: Currency): Money {
        val exchangeRate = getExchangeRate(currency);
        return Money(this.value * exchangeRate, currency)
    }

    private fun getExchangeRate(currency: Currency): BigDecimal {
        // Returns a random number as exchange rate for currency conversion
        return BigDecimal(Random.nextDouble(0.1, 100.0) * currency.ordinal);
    }
}
