package fund.cyber.markets.storer.service

import fund.cyber.markets.common.model.OrderBook
import fund.cyber.markets.common.model.TokensPair
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

const val ORDERBOOK_PATH = "/orderbook"
const val EXCHANGES_SET_PATH = "/exchanges"
const val EXCHANGE_TOKENS_PAIRS_PATH = "/exchange/{exchangeName}/pairs"

@Service
class ConnectorService {
    private val log = LoggerFactory.getLogger(javaClass)!!

    @Autowired
    private lateinit var connectorApiUrl: String

    private val restTemplate = RestTemplate()

    fun getExchanges(): Set<String>? {
        val requestUri = connectorApiUrl + EXCHANGES_SET_PATH

        var exchanges: Array<String>? = null
        try {
            exchanges = restTemplate.getForObject<Array<String>>(requestUri, Array<String>::class.java)
        } catch (e: HttpClientErrorException) {
            log.error("Cannot get list of connected exchanges", e)
        }

        return exchanges?.toSet()
    }

    fun getTokensPairsByExchange(exchange: String): Set<TokensPair>? {
        val requestUri = connectorApiUrl + EXCHANGE_TOKENS_PAIRS_PATH

        val parameters = mutableMapOf<String, String>().apply {
            put("exchangeName", exchange)
        }

        var pairs: Array<TokensPair>? = null
        try {
            pairs = restTemplate.getForObject<Array<TokensPair>>(requestUri, Array<TokensPair>::class.java, parameters)
        } catch (e: HttpClientErrorException) {
            log.error("Cannot get tokens pairs for $exchange exchange. Status code: {}", e.rawStatusCode)
        }

        return pairs?.toSet()

    }

    fun getOrderBook(exchange: String, pair: TokensPair): OrderBook? {
        val requestUri = connectorApiUrl + ORDERBOOK_PATH
        val pairString = pair.base + "_" + pair.quote

        val builder = UriComponentsBuilder.fromUriString(requestUri)
            .queryParam("exchange", exchange)
            .queryParam("pair", pairString)

        var orderBook: OrderBook? = null
        try {
            orderBook = restTemplate.getForObject<OrderBook>(builder.toUriString(), OrderBook::class.java)
        } catch (e: HttpClientErrorException) {
            log.error("Cannot get order book from $exchange and pair: $pair. Response status code: {}", e.rawStatusCode)
        }

        return orderBook
    }

}