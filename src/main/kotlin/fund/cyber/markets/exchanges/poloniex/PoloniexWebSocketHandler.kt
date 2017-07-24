package fund.cyber.markets.exchanges.poloniex


import fund.cyber.markets.model.poloniex
import fund.cyber.markets.storage.AsyncRethinkDbService
import fund.cyber.markets.webscoket.BasicWebSocketHandler
import fund.cyber.markets.webscoket.TradesAndOrdersUpdatesMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession


open class PoloniexWebSocketHandler(
    private val asyncRethinkDbService: AsyncRethinkDbService,
    metadata: PoloniexMetadata
) : BasicWebSocketHandler(poloniex) {

    private val poloniexMessageParser = PoloniexMessageParser(metadata)

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val jsonMessage = message.payload.toString()
        val exchangeMessage = poloniexMessageParser.parseMessage(jsonMessage)
        when (exchangeMessage) {
            is TradesAndOrdersUpdatesMessage -> {
                if (exchangeMessage.trades.isNotEmpty()) {
                    asyncRethinkDbService.saveTrades(exchangeMessage.trades)
                }
            }
        }
    }
}
