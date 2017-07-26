package fund.cyber.markets.exchanges.bitfinex

import fund.cyber.markets.model.Trade
import fund.cyber.markets.model.bitfinex
import fund.cyber.markets.storage.RethinkDbService
import fund.cyber.markets.webscoket.WebSocketContinuousConnectionManager
import kotlinx.coroutines.experimental.channels.SendChannel
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession

@Component
open class BitfinexWebSocketConnectionManager(
        metadataService: BitfinexExchangeMetadataService, tradesChannel: SendChannel<Trade>
) : WebSocketContinuousConnectionManager<BitfinexMetadata>(
        bitfinex, BitfinexWebSocketHandler(metadataService, tradesChannel), metadataService
) {

    override fun subscribeChannels(session: WebSocketSession, metadata: BitfinexMetadata) {
        metadata.channelSymbolForTokensPair.keys.forEach { pairSymbol -> session.subscribeTradeChannel(pairSymbol) }
    }
}

fun WebSocketSession.subscribeTradeChannel(channelSymbol: String) {
    sendMessage(TextMessage("""{"event":"subscribe","channel":"$channel_trades","symbol":"$channelSymbol"}"""))
}

fun WebSocketSession.subscribeOrderChannel(channelSymbol: String) {
    val message = """{"event":"subscribe","channel":"$channel_orders","symbol":"$channelSymbol",prec: "R0",len: 100}"""
    sendMessage(TextMessage(message))
}