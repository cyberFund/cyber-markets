package fund.cyber.markets.connectors.common.kafka

import fund.cyber.markets.model.OrdersBatch
import fund.cyber.markets.model.Trade
import org.apache.kafka.clients.producer.ProducerRecord


class TradeProducerRecord(trade: Trade): ProducerRecord<String, Trade>("TRADES-${trade.exchange}", trade)

class OrdersUpdateProducerRecord(ordersBatch: OrdersBatch):
        ProducerRecord<String, OrdersBatch>("ORDERS-${ordersBatch.exchange}", ordersBatch)