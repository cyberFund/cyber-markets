package fund.cyber.markets.api.rest

import fund.cyber.markets.api.rest.handler.ExchangesInfoHandler
import fund.cyber.markets.api.rest.handler.RawDataHandler
import fund.cyber.markets.api.rest.handler.TickerHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class ApiRouter(
    private val exchangesInfoHandler: ExchangesInfoHandler,
    private val rawDataHandler: RawDataHandler,
    private val tickerHandler: TickerHandler
) {

    @Bean
    fun routes() = router {

        GET("/exchanges", exchangesInfoHandler::getConnectedExchanges)

        GET("/exchange/{exchangeName}/pairs", exchangesInfoHandler::getPairs)

        GET("/orderbook", rawDataHandler::getOrderBook)

        GET("/trade", rawDataHandler::getTrades)

        GET("ticker", tickerHandler::getTickers)

    }

}