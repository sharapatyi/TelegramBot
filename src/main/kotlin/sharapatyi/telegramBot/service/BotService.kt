package sharapatyi.telegramBot.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import sharapatyi.telegramBot.repository.PrivatRepo

@Service
internal class BotService : TelegramLongPollingBot() {

    @Autowired
    private lateinit var privatRepo: PrivatRepo

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
            if (message.text == "Мерчант" && update.message.from.id == 436961179) {
                val request = privatRepo.loadCardCount()
                val responseText =
                    "RESPONSE = ${request.error}"
                sendNotification(chatId, responseText)
            }
            else{
                sendNotification(chatId, "Access Denied")
            }
        }
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
}
