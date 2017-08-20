package fund.cyber.markets.connectors.hitbtc

import fund.cyber.markets.connectors.common.HITBTC_WS_ENDPOINT
import fund.cyber.markets.connectors.common.PairsProvider
import fund.cyber.markets.connectors.common.ws.ExchangeMessageParser
import fund.cyber.markets.connectors.common.ws.OrdersWsEndpoint
import fund.cyber.markets.connectors.common.ws.TradesWsEndpoint
import fund.cyber.markets.model.TokensPair
import java.math.BigDecimal

class HitBtcTokensPair(
        val symbol: String,
        val lotSize: BigDecimal,
        val priceStep: BigDecimal,
        base: String,
        quote: String
) : TokensPair(base, quote)

class HitBtcTradesEndpoint: TradesWsEndpoint(HITBTC_WS_ENDPOINT) {

    @Suppress("UNCHECKED_CAST")
    val channelSymbolForTokensPairsHitBtc = channelSymbolForTokensPairs as HashMap<String, HitBtcTokensPair>

    override val name: String = "HitBtc Trades"
    override val messageParser: ExchangeMessageParser = HitBtcTradesMessageParser(channelSymbolForTokensPairsHitBtc)
    override val pairsProvider: PairsProvider = HitBtcPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String = ""
}

class HitBtcOrdersEndpoint: OrdersWsEndpoint(HITBTC_WS_ENDPOINT) {

    @Suppress("UNCHECKED_CAST")
    val channelSymbolForTokensPairsHitBtc = channelSymbolForTokensPairs as HashMap<String, HitBtcTokensPair>

    override val name: String = "HitBtc Orders"
    override val messageParser: ExchangeMessageParser = HitBtcOrdersMessageParser(channelSymbolForTokensPairsHitBtc)
    override val pairsProvider: PairsProvider = HitBtcPairsProvider()

    override fun getSubscriptionMsgByChannelSymbol(pairSymbol: String): String = ""
}
