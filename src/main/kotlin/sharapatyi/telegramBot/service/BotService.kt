package sharapatyi.TelegramBot.service


import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.apache.http.HttpConnection
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.protocol.HTTP
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import sharapatyi.telegramBot.service.DataDto
import sharapatyi.telegramBot.service.MerchantDto
import sharapatyi.telegramBot.service.PaymentDto
import sharapatyi.telegramBot.service.request
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.http.HttpClient


@Service
class BotService : TelegramLongPollingBot() {

    @Value("\${telegram.requestId}")
    private val id = 0

    @Value("\${telegram.requestSignature}")
    private val signature = ""

    @Value("\${telegram.requestCardNum}")
    private val cardNum: Long = 0

    @Value("\${telegram.botName}")
    private val botName: String = ""

    @Value("\${telegram.token}")
    private val token: String = ""

    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    /**
     *  Обработка запросов от пользователя
     *
     * @param update - объект
     */
    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            val message = update.message
            val chatId = message.chatId
            val responseText = if (message.hasText()) {
                when (val messageText = message.text) {
                    "/start" -> "Добро пожаловать!"
                    else -> "*$messageText*"
                }
            } else {
                "Я понимаю только текст"
            }
            if (message.text == "Мерчант" && update.message.from.id == 436961179) {
                val dto = request(
                    merchant = MerchantDto(
                        id = id,
                        signature = signature
                    ),
                    data = DataDto(
                        oper = "cmt",
                        wait = 0,
                        test = 0,
                        payment = PaymentDto(
                            propnameCardNum = cardNum,
                            propnameCountry = "UA"
                        )
                    )
                )
                sendFormToPrivat(dto)
                val responseText =
                    "id = ${dto.merchant.id}; signature = ${dto.merchant.signature}, card number = ${dto.data.payment.propnameCardNum}"
                sendNotification(chatId, responseText)
            }
            sendNotification(chatId, responseText)
        }
    }

    private fun sendFormToPrivat(dto: request) {
        val xmlMapper = XmlMapper(
            JacksonXmlModule().apply { setDefaultUseWrapper(false) }
        ).apply {
            enable(SerializationFeature.INDENT_OUTPUT)
            enable(SerializationFeature.WRAP_ROOT_VALUE)
        }
        val dto = request(
            merchant = MerchantDto(
                id = id,
                signature = signature
            ),
            data = DataDto(
                oper = "cmt",
                wait = 0,
                test = 0,
                payment = PaymentDto(
                    propnameCardNum = cardNum,
                    propnameCountry = "UA"
                )
            )
        )
        val xml = xmlMapper.writeValueAsString(dto)

//        val mURL = URL("https://api.privatbank.ua/p24api/balance")
//        with(mURL.openConnection() as HttpURLConnection){
//            requestMethod = "POST"
//            BufferedReader(InputStreamReader(inputStream)).use {
//                val response = StringBuffer()
//                var inputLine = it.readLine()
//                while (inputLine!=null){
//                    response.append(inputLine)
//                    inputLine = it.readLine()
//                }
//                it.close()
//                println("Response : $response")
//            }
//        }

        val url = URL("https://api.privatbank.ua/p24api/balance")
        val httpConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        httpConnection.requestMethod = "POST"
        httpConnection.setRequestProperty("Content-Type", "application/xml")
        httpConnection.setRequestProperty("Accept", "application/xml")
        httpConnection.doOutput = true
        val outStream: OutputStream = httpConnection.outputStream
        val outStreamWriter = OutputStreamWriter(outStream, "UTF-8")
        outStreamWriter.write(xml)
        outStreamWriter.flush()
        outStreamWriter.close()

        println(httpConnection.responseCode)
        println(httpConnection.responseMessage)

        val inputStream:InputStream = httpConnection.inputStream
        BufferedReader(InputStreamReader(inputStream)).use {
            val response = StringBuffer()
            var inputLine = it.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = it.readLine()
            }
            it.close()
            println("Response : $response")

        println(xml)
        }
    }

    private fun sendNotification(chatId: Long, responseText: String) {
        val responseMessage = SendMessage(chatId, responseText)
        responseMessage.setParseMode("Markdown")
        val markup = ReplyKeyboardMarkup()
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()
        row.add("Мерчант")
        row.add("Мерчант-физлицо")
        keyboard.add(row)
        markup.keyboard = keyboard
        responseMessage.replyMarkup = markup
        execute(responseMessage)
    }

}