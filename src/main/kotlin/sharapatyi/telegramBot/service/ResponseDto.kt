package sharapatyi.telegramBot.service

import java.util.*

data class ResponseDto(
    val id: Int?,
    val signature: String?,
    val account: Long?,
    val cardNumber: Long?,
    val accName: String?,
    val accType: String?,
    val currency: String?,
    val cardType: String?,
    val mainCardNumber: Long?,
    val cardStatus: String?,
    val avBalance: Double?,
    val balDate: Date?,
    val balance: Long?,
    val limit: Double?
)
