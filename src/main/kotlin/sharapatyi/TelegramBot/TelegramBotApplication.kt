package sharapatyi.TelegramBot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.ApiContextInitializer

@SpringBootApplication
class TelegramBotApplication

fun main(args: Array<String>) {
	ApiContextInitializer.init() // инициализация контекста для Telegram API
	runApplication<TelegramBotApplication>(*args)
}
