package fund.cyber.markets.ticker.processor

import fund.cyber.markets.common.closestSmallerMultiply
import fund.cyber.markets.common.model.BaseTokens
import fund.cyber.markets.common.model.Exchanges
import fund.cyber.markets.common.model.TickerPrice
import fund.cyber.markets.common.model.TokenPrice
import fund.cyber.markets.common.model.TokenTicker
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.ticker.common.CrossConversion
import fund.cyber.markets.ticker.processor.price.PriceProcessor
import fund.cyber.markets.ticker.service.TickerService
import fund.cyber.markets.ticker.service.TokenPriceService
import fund.cyber.markets.ticker.service.TradeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*

@Component
class HistoricalTickerProcessor(
    private val tradeService: TradeService,
    private val tickerService: TickerService,
    private val tokenPriceService: TokenPriceService,
    @Qualifier("windowIntervals")
    private val windowIntervals: Set<Long>,
    private val crossConversion: CrossConversion,
    private val lagFromRealTime: Long,
    private val priceProcessors: List<PriceProcessor>
) {
    private val log = LoggerFactory.getLogger(HistoricalTickerProcessor::class.java)!!

    private val tickers = mutableMapOf<String, TokenTicker>()

    fun start() {
        log.info("Start historical ticker processor. Timestamp: ${Date()}")

        val startTimestamp = tradeService.getLastProcessedTimestamp().block()!!.value

        log.info("Last processed timestamp: ${Date(startTimestamp)}")

        var lastTimestamp = startTimestamp
        val currentTimestamp = System.currentTimeMillis()

        windowIntervals.forEach { interval ->

            crossConversion.invalidatePrices()
            var timestampFrom = closestSmallerMultiply(startTimestamp - interval, interval)
            var timestampTo = timestampFrom + interval

            while (timestampTo < currentTimestamp - lagFromRealTime) {

                tickers.clear()
                val trades = tradeService
                    .getTrades(timestampFrom, timestampTo)
                    .collectList()
                    .defaultIfEmpty(mutableListOf())
                    .block()!!
                    .sortedBy { trade -> trade.timestamp }

                if (trades.isNotEmpty()) {
                    log.debug("Start timestamp: $timestampFrom. Interval: $interval. Trades count: ${trades.size}")

                    crossConversion.updateMapOfPrices(trades)

                    for (trade in trades) {
                        updateVolumes(trade, timestampFrom, interval)

                        BaseTokens.values().forEach { baseToken ->
                            updateBaseVolumesWithPrices(baseToken.name, trade, timestampFrom, interval)
                        }
                    }

                    tickerService.save(tickers.values).collectList().block()
                    tokenPriceService.save(getTokenPrices(tickers.values.toList())).collectList().block()
                }

                if (timestampTo > lastTimestamp) {
                    lastTimestamp = timestampTo
                }
                timestampFrom = timestampTo
                timestampTo += interval
            }

        }

        tradeService.updateLastProcessedTimestamp(lastTimestamp).block()
        log.info("Successfully completed tickers calculation. Timestamp: ${Date()} ")
    }

    private fun getTokenPrices(tickers: List<TokenTicker>): MutableList<TokenPrice> {
        val prices = mutableListOf<TokenPrice>()

        priceProcessors.forEach { processor ->
            prices.addAll(processor.calculate(tickers))
        }

        return prices
    }

    private fun updateVolumes(trade: Trade, timestampFrom: Long, interval: Long) {
        val base = trade.pair.base
        val quote = trade.pair.quote
        val exchange = trade.exchange
        val timestampTo = timestampFrom + interval

        val tickerBase = tickers
            .getOrPut(base, {
                TokenTicker(base, timestampFrom, timestampTo, interval)
            })
        val tickerQuote = tickers
            .getOrPut(quote, {
                TokenTicker(quote, timestampFrom, timestampTo, interval)
            })

        val volumeBase = tickerBase.volume
            .getOrPut(quote, { mutableMapOf() })
            .getOrPut(exchange, { BigDecimal.ZERO })
            .plus(trade.baseAmount)
        tickerBase.volume[quote]!![exchange] = volumeBase

        val volumeBaseAll = tickerBase.volume
            .getOrPut(quote, { mutableMapOf() })
            .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
            .plus(trade.baseAmount)
        tickerBase.volume[quote]!![Exchanges.ALL] = volumeBaseAll

        val volumeQuote = tickerQuote.volume
            .getOrPut(base, { mutableMapOf() })
            .getOrPut(exchange, { BigDecimal.ZERO })
            .plus(trade.quoteAmount)
        tickerQuote.volume[base]!![exchange] = volumeQuote

        val volumeQuoteAll = tickerQuote.volume
            .getOrPut(base, { mutableMapOf() })
            .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
            .plus(trade.quoteAmount)
        tickerQuote.volume[base]!![Exchanges.ALL] = volumeQuoteAll
    }

    private fun updateBaseVolumesWithPrices(baseTokenSymbol: String, trade: Trade, timestampFrom: Long, interval: Long) {
        val base = trade.pair.base
        val quote = trade.pair.quote
        val exchange = trade.exchange
        val timestampTo = timestampFrom + interval

        val tickerBase = tickers
            .getOrPut(base, {
                TokenTicker(base, timestampFrom, timestampTo, interval)
            })
        val tickerQuote = tickers
            .getOrPut(quote, {
                TokenTicker(quote, timestampFrom, timestampTo, interval)
            })

        val priceForBase = crossConversion.calculate(base, baseTokenSymbol, exchange) ?: BigDecimal.ZERO
        val priceForQuote = crossConversion.calculate(quote, baseTokenSymbol, exchange) ?: BigDecimal.ZERO

        val basePrice = tickerBase.price
            .getOrPut(baseTokenSymbol, { mutableMapOf() })
            .getOrPut(exchange, { TickerPrice(priceForBase) })
        tickerBase.price[baseTokenSymbol]!![exchange] = basePrice.update(priceForBase)

        val quotePrice = tickerQuote.price
            .getOrPut(baseTokenSymbol, { mutableMapOf() })
            .getOrPut(exchange, { TickerPrice(priceForQuote) })
        tickerQuote.price[baseTokenSymbol]!![exchange] = quotePrice.update(priceForQuote)

        val volumeBaseBCT = tickerBase.baseVolume
            .getOrPut(baseTokenSymbol, { mutableMapOf() })
            .getOrPut(exchange, { BigDecimal.ZERO })
            .plus(priceForBase.multiply(trade.baseAmount))
        tickerBase.baseVolume[baseTokenSymbol]!![exchange] = volumeBaseBCT

        val volumeBaseBCTall = tickerBase.baseVolume
            .getOrPut(baseTokenSymbol, { mutableMapOf() })
            .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
            .plus(priceForBase.multiply(trade.baseAmount))
        tickerBase.baseVolume[baseTokenSymbol]!![Exchanges.ALL] = volumeBaseBCTall

        val volumeQuoteBCT = tickerQuote.baseVolume
            .getOrPut(baseTokenSymbol, { mutableMapOf() })
            .getOrPut(exchange, { BigDecimal.ZERO })
            .plus(priceForQuote.multiply(trade.quoteAmount))
        tickerQuote.baseVolume[baseTokenSymbol]!![exchange] = volumeQuoteBCT

        val volumeQuoteBCTall = tickerQuote.baseVolume
            .getOrPut(baseTokenSymbol, { mutableMapOf() })
            .getOrPut(Exchanges.ALL, { BigDecimal.ZERO })
            .plus(priceForQuote.multiply(trade.quoteAmount))
        tickerQuote.baseVolume[baseTokenSymbol]!![Exchanges.ALL] = volumeQuoteBCTall
    }

}