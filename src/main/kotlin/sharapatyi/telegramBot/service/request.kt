package sharapatyi.telegramBot.service


data class request(
    val merchant:MerchantDto,
    val data:DataDto
)

class DataDto(
    val oper: String,
    val wait: Int,
    val test: Int,
    val payment: PaymentDto

)

class PaymentDto(
    val propnameCardNum:Long,
    val propnameCountry:String,
)

class MerchantDto (
val id:Int,
val signature: String
)
