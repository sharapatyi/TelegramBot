package sharapatyi.TelegramBot.service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import sharapatyi.telegramBot.service.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

@Service
class BotService : TelegramLongPollingBot() {

    @Value("\${telegram.requestCardNum}")
    private val cardNum = ""

    @Value("\${telegram.requestId}")
    private val merchantId = 0

    @Value("\${telegram.requestSignature}")
    private val merchantSignature = ""

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
                val url = URL("https://api.privatbank.ua/p24api/balance")
                val dto = RequestDto(
                    merchant = MerchantDto(
                        id = merchantId,
                        signature = merchantSignature
                    ),
                    data = DataDto()
                )
                getResponseFromPrivat(url, createXmlForm(dto))
                val responseText =
                    "id = ${dto.merchant.id}; signature = ${dto.merchant.signature}, card number = ${dto.data.payment}"
                sendNotification(chatId, responseText)
            }
            sendNotification(chatId, responseText)
        }
    }

    /**
     * Создание xml формы
     *
     * @param dto = RequestDto
     * @return
     */
    private fun createXmlForm(dto: RequestDto): String {
        val xmlMapper = XmlMapper(
            JacksonXmlModule().apply { setDefaultUseWrapper(false) }
        ).apply {
            enable(SerializationFeature.INDENT_OUTPUT)
            enable(SerializationFeature.WRAP_ROOT_VALUE)
        }
        val xml = xmlMapper.writeValueAsString(dto)
        println(xml)
        return xml
    }

    /**
     * Метод для отправки сообщений по нажатию на кнопку
     *
     * @param chatId
     * @param responseText
     */
    private fun sendNotification(chatId: Long, responseText: String) {
        val responseMessage = SendMessage(chatId, responseText)
        responseMessage.setParseMode("Markdown")
        val markup = ReplyKeyboardMarkup()
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()
        row.add("Мерчант")
        keyboard.add(row)
        markup.keyboard = keyboard
        responseMessage.replyMarkup = markup
        execute(responseMessage)
    }

    /**
     * Метод для отправки запроса на api Приватбанка и получение response
     *
     * @param url = "https://api.privatbank.ua/p24api/balanc"
     * @param xml = форма, которую строим в функции createXmlForm
     */
    private fun getResponseFromPrivat(url: URL, xml: String) {
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

        val inputStream: InputStream = httpConnection.inputStream
        BufferedReader(InputStreamReader(inputStream)).use {
            val response = StringBuffer()
            var inputLine = it.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = it.readLine()
            }
            it.close()
            println("Response : $response")
        }
    }
}