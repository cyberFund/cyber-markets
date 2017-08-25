package fund.cyber.markets.api.trades

import fund.cyber.markets.model.TokensPair
import java.util.LinkedList


class TradesBroadcastersIndex : TradesChannelsIndexUpdateListener {

    private val index: MutableMap<TokensPair, MutableMap<String, TokensPairTradesBroadcaster>> = HashMap()

    override fun newTradesChannel(exchange: String, pair: TokensPair, channel: TradesChannel) {
        val broadcaster = TokensPairTradesBroadcaster(channel)
        index.getOrPut(pair, { HashMap() }).put(exchange, broadcaster)
    }

    fun broadcastersForPairs (pairs: List<TokensPair>): Collection<TokensPairTradesBroadcaster> {
        return index
                .filter { (pair, _) -> pairs.contains(pair) }
                .flatMap { (_, pairIndex) -> pairIndex.values }
    }

    fun broadcastersFor(pairs: List<TokensPair>, exchanges: List<String> ): Collection<TokensPairTradesBroadcaster> {
        return index
                .filter { (pair, _) -> pairs.contains(pair) }
                .flatMap { (_, pairIndex) -> pairIndex.entries }
                .filter { (exchange, _) -> exchanges.contains(exchange)}
                .map { (_, broadcaster) -> broadcaster }
    }

    fun broadcastersForExchanges (exchanges: List<String>): Collection<TokensPairTradesBroadcaster> {
        return index
                .flatMap { (_, pairIndex) -> pairIndex.entries }
                .filter { (exchange, _) -> exchanges.contains(exchange)}
                .map { (_, broadcaster) -> broadcaster }
    }

    fun broadcastersForAll (): Collection<TokensPairTradesBroadcaster> {
        return index
                .flatMap { (_, pairIndex) -> pairIndex.entries }
                .map { (_, broadcaster) -> broadcaster }
    }

    fun broadcastersGetAllPairs (): Collection<TokensPair>{
        return index.keys
    }

    fun broadcastersGetExchangesTree (): MutableMap<String, List<TokensPair>>{
        var resultMap: HashMap<String, List<TokensPair>> = hashMapOf()
        val indexCopy = index
        val flatMap = indexCopy.flatMap { (_, pairIndex) -> pairIndex.entries }
        flatMap.forEach { exc ->resultMap.put(exc.key, getAllPairsForExchange(exc.key))}
        return resultMap
    }

    private fun getAllPairsForExchange (exchange: String): MutableList<TokensPair> {
        val indexCopy = index
        var resultPairsList : LinkedList<TokensPair> = LinkedList()
        for (el in indexCopy) {
            if(el.value.keys.contains(exchange)){
                resultPairsList.add(el.key)
            }
        }
        return resultPairsList
    }
}