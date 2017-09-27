package fund.cyber.markets.connectors.common

import fund.cyber.markets.model.Order
import fund.cyber.markets.model.OrdersBatch
import fund.cyber.markets.model.Trade


/**
 * Represents parsed message from exchange.
 */
open class ExchangeMessage

open class NotParsedExchangeMessage : ExchangeMessage()

/**
 * Represents unknown format message obtained from exchange.
 */
open class UnknownFormatMessage(
        val message: String
) : NotParsedExchangeMessage()

/**
 * Represents right structured message with unknown tokens pair.
 * Symbol -> either channelId or channelSymbol
 */
open class ContainingUnknownTokensPairMessage(
        val symbol: String
) : NotParsedExchangeMessage()

/**
 * Represents trades and orders updates received from exchange.
 */
data class TradesUpdatesMessage(
        val trades: List<Trade> = ArrayList()
) : ExchangeMessage()

enum class OrdersUpdateType {
    FULL_ORDER_BOOK, COMMON
}

data class OrdersUpdatesMessage(
        val type: OrdersUpdateType,
        val baseToken: String,
        val quoteToken: String,
        val exchange: String,
        val orders: List<Order> = ArrayList()
) : ExchangeMessage() {
    val ordersBatch = OrdersBatch(baseToken, exchange, quoteToken, orders)
}