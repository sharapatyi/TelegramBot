package v_sharapatyi.TelegramBot.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class BotService: TelegramLongPollingBot() {

    @Value("\${telegram.botName}")
    private val botName:String = ""

    @Value("\${telegram.token}")
    private val token:String = ""

    override fun getBotUsername(): String = botName

    override fun getBotToken(): String = token

    override fun onUpdateReceived(update: Update?) {
        TODO("Not yet implemented")
    }


}