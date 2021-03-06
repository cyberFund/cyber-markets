package fund.cyber.markets.cassandra.common

import fund.cyber.markets.cassandra.model.CqlOrderBook
import fund.cyber.markets.cassandra.model.CqlOrderSummary
import fund.cyber.markets.cassandra.model.CqlTokenPrice
import fund.cyber.markets.cassandra.model.CqlTokenTicker
import fund.cyber.markets.cassandra.model.CqlTokensPair
import fund.cyber.markets.cassandra.model.CqlTrade
import fund.cyber.markets.cassandra.model.CqlTradeTemporary
import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.OrderSummary
import fund.cyber.markets.common.model.OrderType
import fund.cyber.markets.common.model.TickerPrice
import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.common.model.TokensPair
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.common.model.TradeType

fun CqlTokensPair.toTokensPair(): TokensPair {
    return TokensPair(
        base = this.base,
        quote = this.quote
    )
}

fun CqlTrade.toTrade(): Trade {
    return Trade(
        exchange = this.exchange,
        pair = this.pair.toTokensPair(),
        type = TradeType.valueOf(this.type),
        timestamp = this.timestamp.time,
        epochMinute = this.epochMinute,
        tradeId = this.tradeId,
        baseAmount = this.baseAmount,
        quoteAmount = this.quoteAmount,
        price = this.price
    )
}

fun CqlTradeTemporary.toTrade(): Trade {
    return Trade(
        exchange = this.exchange,
        pair = this.pair.toTokensPair(),
        type = TradeType.valueOf(this.type),
        timestamp = this.timestamp.time,
        epochMinute = this.epochMinute,
        tradeId = this.tradeId,
        baseAmount = this.baseAmount,
        quoteAmount = this.quoteAmount,
        price = this.price
    )
}

fun CqlOrderBook.toOrderBook(): OrderBook {
    return OrderBook(
        timestamp = this.timestamp.time,
        asks = this.asks.map { order -> order.toOrderSummary() }.toMutableList(),
        bids = this.bids.map { order -> order.toOrderSummary() }.toMutableList()
    )
}

fun CqlOrderSummary.toOrderSummary(): OrderSummary {
    return OrderSummary(
        type = OrderType.valueOf(this.type),
        timestamp = this.timestamp.time,
        amount = this.amount,
        price = this.price
    )
}

fun CqlTokenTicker.toTokenTicker(): TokenTicker {
    return TokenTicker(
        symbol = this.symbol,
        timestampFrom = this.timestampFrom.time,
        timestampTo = this.timestampTo.time,
        interval = this.interval,
        price = this.price.mapValues { (_, priceMap) ->
            priceMap.mapValues { (_, price) ->
                TickerPrice(
                    open = price.open,
                    close = price.close,
                    min = price.min,
                    max = price.max
                )
            }.toMutableMap()
        }.toMutableMap(),
        volume = this.volume.value.mapValues { (_, map) -> map.toMutableMap() }.toMutableMap(),
        baseVolume = this.baseVolume.value.mapValues { (_, map) -> map.toMutableMap() }.toMutableMap()
    )
}

fun CqlTokenPrice.toTokenPrice(): TokenPrice {
    return TokenPrice(
        symbol = this.symbol,
        method = this.method,
        timestampFrom = this.timestampFrom,
        timestampTo = this.timestampTo,
        values = this.values.mapValues { (_, value) -> value.toMutableMap()  }.toMutableMap()
    )
}