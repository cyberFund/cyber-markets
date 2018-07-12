package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.model.CqlTokenPrice
import fund.cyber.markets.cassandra.repository.TokenPriceRepository
import fund.cyber.markets.common.model.TokenPrice
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class TokenPriceService(
    private val tokenPriceRepository: TokenPriceRepository
) {
    private val log = LoggerFactory.getLogger(TokenPriceService::class.java)!!

    fun save(prices : MutableCollection<TokenPrice>): Flux<CqlTokenPrice> {
        log.info("Saving token prices. Count: ${prices.size}")

        return tokenPriceRepository.saveAll(prices.map { CqlTokenPrice(it) })
    }

}