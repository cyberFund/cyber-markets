package fund.cyber.markets.ticker.processor

import fund.cyber.markets.common.model.TokenTicker

interface TickerProcessor {
    fun process()
    fun update(hopTickers: MutableMap<String, TokenTicker>)
    fun saveAndProduceToKafka()
    fun updateTimestamps()
}