package sharapatyi.TelegramBot.service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import sharapatyi.TelegramBot.repository.PrivatRepo

@Service
internal class BotService : TelegramLongPollingBot() {

    @Autowired private lateinit var privatRepo: PrivatRepo

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
                val request = privatRepo.loadCardCount()
                val responseText =
                    "RESPONSE = ${request.elseInfo}"
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
        markup.resizeKeyboard = true
        val keyboard: MutableList<KeyboardRow> = ArrayList()
        val row = KeyboardRow()
        row.add("Мерчант")
        keyboard.add(row)
        markup.keyboard = keyboard
        responseMessage.replyMarkup = markup
        execute(responseMessage)
    }

    /**
     * Отправка запроса на апи приватбанка и получение респонса
     *
     * @param xml
     * @return
     */
    private fun getResponseFromPrivat(xml: String): String {
        val client = WebClient.create()
        val response: WebClient.ResponseSpec = client.post()
            .uri("https://api.privatbank.ua/p24api/balance")
            .header("Accept", "application/xml")
            .header("Content-Type", "application/xml")
            .bodyValue(xml)
            .retrieve()
        val responseBody = response.bodyToMono(String::class.java).block()
        println("RESPONSE : $responseBody")
        return responseBody!!
    }
}
