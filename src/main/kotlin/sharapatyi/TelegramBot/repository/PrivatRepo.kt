package sharapatyi.TelegramBot.repository

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PrivatRepo {

    @Value("\${telegram.requestId}")
    private val merchantId = ""

    @Value("\${telegram.requestSignature}")
    private val merchantSignature = ""

    @Value("\${telegram.requestCardNum}")
    private val cardNum = ""

    /**
     * Отправка запроса на апи приватбанка и получение респонса
     *
     * @param xml
     * @return
     */
    fun loadCardCount(): PrivatCardDto {
        val xml = buildRequest()
        val client = WebClient.create()
        val response: WebClient.ResponseSpec = client.post()
            .uri("https://api.privatbank.ua/p24api/balance")
            .header("Accept", "application/xml")
            .header("Content-Type", "application/xml")
            .bodyValue(xml)
            .retrieve()
        val responseBody = response.bodyToMono(String::class.java).block()
        println(responseBody)
        return parseResponse(responseBody!!)
    }

    /**
     * Строим тело запроса
     *
     * @return
     */
    private fun buildRequest(): String {
        val requestDto = """
            <?xml version="1.0" encoding="UTF-8"?>
            <request version="1.0">
                <merchant>
                    <id>$merchantId</id>
                    <signature>$merchantSignature</signature>
                </merchant>
                <data>
                    <oper>cmt</oper>
                    <wait>0</wait>
                    <test>0</test>
                    <payment id="">
                    <prop name="cardnum" value="$cardNum" />
                    <prop name="country" value="UA" />
                    </payment>
                </data>
            </request>
        """.trimIndent()
        println(requestDto)
        return requestDto
    }

    /**
     * Строим модель ответа
     *
     * @param resp
     * @return
     */
    private fun parseResponse(resp: String): PrivatCardDto {
        val error = resp.lines()
            .first { it.contains("<error") }
            .replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?><response version=\"1.0\"><data><", "")
            .replace(" /></data></response>", "")
            .trim()
        val response = (resp.lines()
            .first { it.contains("<error") }
            .replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?><response version=\"1.0\"><data><", "")
            .replace(" =\"invalid signature\" /></data></response>", "")
            .trim())
        if (response == "error message") {
            return PrivatCardDto(
                elseInfo = "$error"
            )
        }
        else return PrivatCardDto(
            cardNumber = resp.lines()
                .first { it.contains("<card_number") }
                .replace("<card_number", "")
                .replace("</card_number", "")
                .trim(),
            currency = resp.lines()
                .first { it.contains("<currency") }
                .replace("<currency", "")
                .replace("</currency", "")
                .trim(),
            count = resp.lines()
                .first { it.contains("<balance") }
                .replace("<balance", "")
                .replace("</balance", "")
                .trim())
    }

    data class PrivatCardDto(
        val cardNumber: String? = null,
        val currency: String? = null,
        val count: String? = null,
        val elseInfo: String? = null
    )
}