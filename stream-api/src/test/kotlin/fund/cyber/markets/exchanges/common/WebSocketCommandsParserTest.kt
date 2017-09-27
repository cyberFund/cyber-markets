package fund.cyber.markets.exchanges.common

import fund.cyber.markets.api.common.ChannelSubscriptionCommand
import fund.cyber.markets.api.common.IncomingMessageSubscribeTopicType
import fund.cyber.markets.api.common.UnknownCommand
import fund.cyber.markets.api.common.WebSocketCommandsParser
import fund.cyber.markets.model.TokensPairInitializer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test


@DisplayName("Websocket Commands Parser Test:")
class WebSocketCommandsParserTest {

    private val commandsParser = WebSocketCommandsParser()

    @Test
    @DisplayName("Should not throw exception for invalid provided json, but return UnknownMessage")
    fun testInvalidJsonProvided() {

        val message = "[q34342%&$&__~~~~"
        val command = commandsParser.parseMessage(message)

        Assertions.assertTrue(command is UnknownCommand)
        command as UnknownCommand
        Assertions.assertEquals(message, command.message)
    }

    @Test
    @DisplayName("Should parse trades subscription")
    fun testTradeSubscriptionProvided() {

        val pairs = listOf(
                TokensPairInitializer.fromLabel("BTC_ETH", "_"),
                TokensPairInitializer.fromLabel("ETH_USD", "_")
        )

        val message = """{"subscribe":"trades","pairs":["BTC_ETH","ETH_USD"]}"""
        val command = commandsParser.parseMessage(message)

        Assertions.assertTrue(command is ChannelSubscriptionCommand)
        command as ChannelSubscriptionCommand
        Assertions.assertEquals(IncomingMessageSubscribeTopicType.TRADES, command.type)
        Assertions.assertArrayEquals(pairs.toTypedArray(), command.pairs?.toTypedArray())
    }

    @Test
    @DisplayName("Should parse trades subscription and invert pair by alphabet if needed")
    fun testTradeSubscriptionProvidedAndInvertIsCorrect() {

        val pairs = listOf(
                TokensPairInitializer.fromLabel("BTC_ETH", "_"),
                TokensPairInitializer.fromLabel("AAA_BBB", "_")
        )

        val message = """{"subscribe":"trades","pairs":["BTC_ETH","BBB_AAA"]}"""
        val command = commandsParser.parseMessage(message)

        Assertions.assertTrue(command is ChannelSubscriptionCommand)
        command as ChannelSubscriptionCommand
        Assertions.assertEquals(IncomingMessageSubscribeTopicType.TRADES, command.type)
        Assertions.assertArrayEquals(pairs.toTypedArray(), command.pairs?.toTypedArray())
    }

    @Test
    @DisplayName("Should parse trades subscription, invert pair by alphabet and delete equals")
    fun testTradeSubscriptionProvidedInvertIsCorrectAndDeleteEquals() {

        val pairs = listOf(
                TokensPairInitializer.fromLabel("BTC_ETH", "_")
        )

        val message = """{"subscribe":"trades","pairs":["BTC_ETH","ETH_BTC"]}"""
        val command = commandsParser.parseMessage(message)

        Assertions.assertTrue(command is ChannelSubscriptionCommand)
        command as ChannelSubscriptionCommand
        Assertions.assertEquals(IncomingMessageSubscribeTopicType.TRADES, command.type)
        Assertions.assertArrayEquals(pairs.toTypedArray(), command.pairs?.toTypedArray())
    }
}