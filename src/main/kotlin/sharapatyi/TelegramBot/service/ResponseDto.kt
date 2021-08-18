package sharapatyi.TelegramBot.service

data class ResponseDto(
    val account: String?,
    val cardNumber: String?,
    val accName: String?,
    val accType: String?,
    val currency: String?,
    val cardType: String?,
    val mainCardNumber: String?,
    val cardStatus: String?,
    val avBalance: String?,
    val balDate: String?,
    val balance: String?,
    val finLimit: String?,
    val tradeLimit: String
)
