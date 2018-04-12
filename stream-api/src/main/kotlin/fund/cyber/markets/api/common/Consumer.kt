package fund.cyber.markets.api.common

import fund.cyber.markets.api.configuration.KafkaConfiguration
import fund.cyber.markets.api.configuration.ordersTopicNamePattern
import fund.cyber.markets.api.configuration.tickersTopicNamePattern
import fund.cyber.markets.common.kafka.JsonDeserializer
import fund.cyber.markets.common.model.Order
import fund.cyber.markets.common.model.OrdersBatch
import fund.cyber.markets.common.model.Ticker
import fund.cyber.markets.common.model.TickerKey
import fund.cyber.markets.common.model.TokensPairInitializer
import fund.cyber.markets.common.model.Trade
import fund.cyber.markets.ordersSingleThreadContext
import fund.cyber.markets.tickersSingleThreadContext
import fund.cyber.markets.tradesSingleThreadContext
import kotlinx.coroutines.experimental.launch
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer

abstract class Consumer<K, V>(
        val configuration: KafkaConfiguration = KafkaConfiguration(),
        keyDeserializer: Deserializer<K>,
        valueDeserializer: Deserializer<V>,
        groupId: String
) : Runnable {
    val kafkaConsumer = KafkaConsumer(configuration.consumersProperties(groupId), keyDeserializer, valueDeserializer)

    override fun run() {
        kafkaConsumer.use {
            it.subscribe(configuration.topicNamePattern, NoOpConsumerRebalanceListener())
            while (true) {
                val records = it.poll(configuration.poolAwaitTimeout)
                handleNew(records)
            }
        }
    }

    abstract fun handleNew(records: ConsumerRecords<K, V>)

    fun shutdown() {
        kafkaConsumer.wakeup()
    }
}

class OrdersBatchConsumer(
        val channelsIndex: ChannelsIndex<List<Order>>
) : Consumer<String, OrdersBatch>(
        configuration = KafkaConfiguration(topicNamePattern = ordersTopicNamePattern),
        groupId = "orders-1",
        keyDeserializer = StringDeserializer(),
        valueDeserializer = JsonDeserializer(OrdersBatch::class.java)
) {

    override fun handleNew(records: ConsumerRecords<String, OrdersBatch>) {
        launch(ordersSingleThreadContext) {
            records.map { record -> record.value() }
                    .forEach { ordersBatch ->
                        val pair = TokensPairInitializer(ordersBatch.pair.base, ordersBatch.pair.quote)
                        channelsIndex.channelFor(ordersBatch.exchange, pair).send(ordersBatch.orders)
                    }
        }
    }
}

class TradesConsumer(
        val channelsIndex: ChannelsIndex<Trade>
) : Consumer<String, Trade>(
        groupId = "trades-1",
        keyDeserializer = StringDeserializer(),
        valueDeserializer = JsonDeserializer(Trade::class.java)
) {

    override fun handleNew(records: ConsumerRecords<String, Trade>) {
        launch(tradesSingleThreadContext) {
            records.map { record -> record.value() }
                    .forEach { trade ->
                        val pair = TokensPairInitializer(trade.pair.base, trade.pair.quote)
                        channelsIndex.channelFor(trade.exchange, pair).send(trade)
                    }
        }
    }
}

class TickersConsumer(
        val channelsIndex: ChannelsIndex<Ticker>
) : Consumer<TickerKey, Ticker>(
        configuration = KafkaConfiguration(topicNamePattern = tickersTopicNamePattern),
        groupId = "tickers-1",
        keyDeserializer = JsonDeserializer(TickerKey::class.java),
        valueDeserializer = JsonDeserializer(Ticker::class.java)
) {

    override fun handleNew(records: ConsumerRecords<TickerKey, Ticker>) {
        launch(tickersSingleThreadContext) {
            records.map { record -> record.value() }
                    .forEach { ticker ->
                        val pair = TokensPairInitializer(ticker.pair!!.base, ticker.pair!!.quote)
                        channelsIndex.channelFor(ticker.exchange!!, pair, ticker.windowDuration).send(ticker)
                    }
        }
    }
}