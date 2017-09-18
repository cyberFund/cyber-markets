package fund.cyber.markets.model

open class TokensPair(firstCurrency: String, secondCurrency: String) {

    val base: String
    val quote: String
    var dictionary = listOf("BTC", "ETH", "XMR", "USD")

    init {
        val firstImportance = dictionary.indexOf(firstCurrency)
        val secondImportance = dictionary.indexOf(secondCurrency)
        if (firstImportance >= 0 && secondImportance >= 0) {
            if (firstImportance < secondImportance) {
                this.base = firstCurrency
                this.quote = secondCurrency
            } else {
                this.base = secondCurrency
                this.quote = firstCurrency
            }
        } else if (firstImportance >= 0 || secondImportance >= 0) {
            if (firstImportance >= 0) {
                this.base = firstCurrency
                this.quote = secondCurrency
            } else {
                this.base = secondCurrency
                this.quote = firstCurrency
            }
        } else {
            if (firstCurrency.compareTo(secondCurrency, true) < 0) {
                this.base = firstCurrency
                this.quote = secondCurrency
            } else {
                this.base = secondCurrency
                this.quote = firstCurrency
            }
        }
    }

    fun label(delimiter: String = "/"): String {
        return base + delimiter + quote
    }

    override fun hashCode(): Int {
        var result = base.hashCode()
        result = 31 * result + quote.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TokensPair

        if (base != other.base) return false
        if (quote != other.quote) return false

        return true
    }

    companion object {
        fun fromLabel(label: String, delimiter: String = "/"): TokensPair {
            return TokensPair(label.substringBefore(delimiter), label.substringAfter(delimiter))
        }
    }

}