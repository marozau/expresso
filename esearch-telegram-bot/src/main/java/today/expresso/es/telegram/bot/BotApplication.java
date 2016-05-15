package today.expresso.es.telegram.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.BotLogger;
import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

/**
 * Created by im on 5/12/16.
 */
public class BotApplication {

    private static final Logger logger = LoggerFactory.getLogger(BotApplication.class);

    public static void main(String[] args) throws TelegramApiException {

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new TelegramLongPollingBot() {
                @Override
                public String getBotToken() {
                    return "232561070:AAFxh2DgKa3N21xluXZg565lMAnjh_MmdbQ";
                }

                @Override
                public String getBotUsername() {
                    return "EsearchBot";
                }

                @Override
                public void onUpdateReceived(Update update) {
                    logger.info(update.toString());
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}

