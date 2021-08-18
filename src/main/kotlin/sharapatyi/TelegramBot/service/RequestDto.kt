package sharapatyi.TelegramBot.service

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement


@JacksonXmlRootElement(localName = "request")
data class RequestDto(
    val merchant: MerchantDto,
    val data: DataDto
)

data class DataDto(
    val oper: String = "cmt",
    val wait: Int = 0,
    val test: Int = 0,
    @JacksonXmlElementWrapper(useWrapping = false)
    val payment: PaymentDto
)

data class PaymentDto(
    @JacksonXmlProperty(isAttribute = true, localName = "id")
    val id: String = "",
    @JacksonXmlProperty(localName = "prop")
    val prop: List<PaymentProp>
)

data class PaymentProp(
    @JacksonXmlProperty(isAttribute = true, localName = "name")
    val name: String,
    @JacksonXmlProperty(isAttribute = true, localName = "value")
    val value: String,
)

data class MerchantDto(
    val id: Int = 123123,
    val signature: String = "asdasdas"
)
