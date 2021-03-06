package fund.cyber.markets.cassandra.repository

import com.datastax.driver.core.ConsistencyLevel
import fund.cyber.markets.cassandra.model.CqlTokenPrice
import org.springframework.data.cassandra.core.mapping.MapId
import org.springframework.data.cassandra.repository.Consistency
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface TokenPriceRepository : ReactiveCassandraRepository<CqlTokenPrice, MapId> {

    @Consistency(value = ConsistencyLevel.LOCAL_QUORUM)
    @Query("""
        SELECT * FROM markets.token_price
        WHERE tokensymbol=:tokensymbol
            AND epochhour=:epochhour
            AND method=:method
            AND timestampto=:timestmapTo""")
    fun find(@Param("tokensymbol") symbol: String,
             @Param("epochhour") epochHour: Long,
             @Param("method") method: String,
             @Param("timestmapTo") timestampTo: Long): Flux<CqlTokenPrice>
}