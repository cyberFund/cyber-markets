package fund.cyber.markets.connectors.common.ws

import com.fasterxml.jackson.databind.JsonNode
import fund.cyber.markets.connectors.common.ExchangeMessage
import fund.cyber.markets.connectors.common.UnknownFormatMessage
import fund.cyber.markets.connectors.jsonParser


/**
 * Parse exchange messages.
 */
interface ExchangeMessageParser {
    /**
     * Parse message obtained from exchange.
     * Contract:
     * -For invalid json returns [UnknownFormatMessage].
     * -For unknown message returns [UnknownFormatMessage].
     * -For message contains unknown tokens pair returns [ContainingUnknownTokensPairMessage]
     */
    fun parseMessage(message: String): ExchangeMessage
}

abstract class SaveExchangeMessageParser : ExchangeMessageParser {

    override fun parseMessage(message: String): ExchangeMessage {
        try {
            val jsonRoot = jsonParser.readTree(message)
            return parseMessage(jsonRoot) ?: UnknownFormatMessage(message)

        } catch (exception: Exception) {
            return UnknownFormatMessage(message)
        }
    }

    abstract fun parseMessage(jsonRoot: JsonNode): ExchangeMessage?
}