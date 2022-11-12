package net.cbejltpycbl.PozcraftTeleBot.service;

import com.google.common.primitives.Ints;
import lombok.extern.slf4j.Slf4j;
import net.cbejltpycbl.PozcraftTeleBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;

    static final String IDEA_BUTTON = "IDEA_BUTTON", BACK_BUTTON = "BACK_BUTTON", JOIN_BUTTON = "JOIN_BUTTON",
            DONATE_BUTTON = "DONATE_BUTTON";
    static final String
            startMessage = "Привет! Я - бездушная машина, способная принять в дар твои " +
            "<b>гениальные</b> идеи, деньги или резюме для последующего рассмотрения их администрацией " +
            "проекта <b>ПОЦКРАФТ</b> <i>(русская тоска в майнкрафте)</i>" +
            "\n\nИз команд только /start для рестарта бота. <i>Тыкай кнопки</i>" +
            "\n\n<i>(если ты незнаешь что такое поцкрафт, чек группу в вк - vk.com/pozcraft)</i>",
            joinMessage = "Ого! Ты просто кнопки тыкал или реально в команду хочешь?\n" +
                    "(Зарплаты не будет (покрайней мере до тех пор, пока работаем себе в убыток))" +
                    "\n\n<b>КТО НАМ НУЖЕН:</b>" +
                    "\n- Умелец в рисовании текстур" +
                    "\n- Человек заходивший в blockbench больше одного раза" +
                    "\n- Мастер датпакинга/моддинга/держания серверов (технарь)" +
                    "\n- Голос озвучки" +
                    "\n- Пиар-менеджер ИЛИ чувак который сможет стримить/снимать видео по поцкрафту" +
                    "\n- Пивовар (рассматриваются кандидаты только из Твери и Краснодара)" +
                    "\n- НЕЗНАЮ ПРИДУМАЙТЕ САМИ.*" +
                    "\n\n*Если вы считаете, что сможете каким-либо образом вложить положительный вклад в наш проект, " +
                    "ПИШИТЕ \n(Куда? Прям сюда. Ваше письмо вместе с вашим юзернеймом отправится админам)",
            ideaMessage = "Внимательно слушаю! (Опишите вашу идею(чем можно было бы дополнить/улучшить поцкрафт). Она перенаправится разрабам)",
            donateMessage = "Будем очень признательны!" +
            "\n\nDonationAlerts: https://www.donationalerts.com/r/pozcraft_official" +
            "\nили" +
            "\nНа карту: 5536 9141 5835 3395" +
            "\n" +
            "\nПри желании в комментариях к донату можете указать желаемый кастомный айтем/элемент " +
            "брони, он будет добавлен в ресурспак и будет доступен при переименовывании *предмета-нейм*";

    // Реализованно через хэшмапу, т.к. этого достаточно для целей бота.
    // Если переделывать под более крупную организацию, то лучше подключать базы данных.
    private static Map<Long, Integer> zaeb = new HashMap<>();
    private static Map<Long, String> whatWant = new HashMap<>();

    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().toLowerCase();
            long chatId = update.getMessage().getChatId();
            switch (messageText.toLowerCase()) {
                case "/start", "старт", "пуск":
                    sendMessage(chatId, startMessage);
                    log.info("User \"" + update.getMessage().getChat().getUserName() + " (" + chatId + ")" + "\" send /start command");
                    break;

                default:
                    zaeb.merge(update.getMessage().getChatId(), 1, (x,y) -> x + y);

                    if (whatWant.containsKey(chatId) && whatWant.get(chatId) == "IDEA") {
                        sendToAdmins(update, "Новая идея от @");
                        sendMessage(chatId, "Готово! Ваша идея принята на рассмотрение!");
                    } else if (whatWant.containsKey(chatId) && whatWant.get(chatId) == "JOB") {
                        sendToAdmins(update, "Новое резюме от @");
                        sendMessage(chatId, "Готово! Ваша вакансия принята на рассмотрение!");
                    } else if (zaeb.containsKey(chatId) && zaeb.get(chatId) == 3) {
                        sendMessage(chatId, "че? мой словарный запас 3 команды, я тя непонимаю. тыкай кнопки." +
                            "\n\n<i>(Я тебе больше скажу - я вцелом слова не понимаю. Я просто вижу цифорки и сравниваю их" +
                            " с тем что в меня вписал всевышний Альберт. И когда нахожу совпадения в цифорках," +
                            " передергиваю \"тумблер\", отправляющий заранее им написанные высеры, типа этого)</i>");
                    } else if (zaeb.containsKey(chatId) && zaeb.get(chatId) == 4) {
                        sendMessage(chatId, "даун?");
                    } else if (zaeb.containsKey(chatId) && zaeb.get(chatId) == 6) {
                        sendMessage(chatId, "Не, я понимаю что тебе наверно весело... ХОТЯ НЕТ. НЕ ПОНИМАЮ. НИ СЛОВА");
                    } else if (zaeb.containsKey(chatId) && zaeb.get(chatId) >= 7) {
                        zaeb.put(chatId, 0);
                    } else if (zaeb.containsKey(chatId) && zaeb.get(chatId) <= 2){
                        sendMessage(chatId, "че? мой словарный запас 3 команды, я тя непонимаю. тыкай кнопки.");
                    }
            }
        } else if(update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            switch(callbackData) {
                case IDEA_BUTTON:
                    executeEditMessageTextWithMemory(chatId, messageId, ideaMessage, "IDEA");
                    break;
                case BACK_BUTTON:
                    executeEditMessageText(chatId, messageId, startMessage, "");
                    break;
                case JOIN_BUTTON:
                    executeEditMessageTextWithMemory(chatId, messageId, joinMessage, "JOB");
                    break;
                case DONATE_BUTTON:
                    executeEditMessageText(chatId, messageId, donateMessage, BACK_BUTTON);
                    break;
            }
        }
    }

    private static InlineKeyboardMarkup standardKeyboard() { //выдает стандартный набор кнопок
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var ideaButton = new InlineKeyboardButton();
        ideaButton.setText("Предложить идею \uD83D\uDCA1");
        ideaButton.setCallbackData(IDEA_BUTTON);
        rowInLine.add(ideaButton);
        rowsInLine.add(rowInLine);

        rowInLine = new ArrayList<>();
        var joinInDevButton = new InlineKeyboardButton();
        joinInDevButton.setText("Вступить в команду \uD83E\uDD1D");
        joinInDevButton.setCallbackData(JOIN_BUTTON);
        var donateButton = new InlineKeyboardButton();
        donateButton.setText("Поддержать проект \uD83E\uDD1D");
        donateButton.setCallbackData(DONATE_BUTTON);
        rowInLine.add(joinInDevButton);
        rowInLine.add(donateButton);
        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    private static InlineKeyboardMarkup goBackKeyboard() { //выдает кнопку НАЗАД
        InlineKeyboardMarkup markupInLine = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();

        var ideaButton = new InlineKeyboardButton();
        ideaButton.setText("Назад \uD83D\uDD19");
        ideaButton.setCallbackData(BACK_BUTTON);
        rowInLine.add(ideaButton);
        rowsInLine.add(rowInLine);

        markupInLine.setKeyboard(rowsInLine);
        return markupInLine;
    }

    private void executeEditMessageText(long chatId, long messageId, String messageText, String keyboard) { //редактирует сообщение
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId((int) messageId);
        message.setParseMode(ParseMode.HTML);
        message.setText(messageText);

        switch (keyboard) {
            case BACK_BUTTON:
                message.setReplyMarkup(goBackKeyboard());
                break;
            default:
                whatWant.put(chatId, "");
                message.setReplyMarkup(standardKeyboard());
                break;
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage() + " (ошибка при отправке сообщения)");
        }
    }

    private void executeEditMessageTextWithMemory(long chatId, long messageId, String messageText, String whatIs) { //редактирует сообщение + запоминает на какой вкладке находится пользователь
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId((int) messageId);
        message.setParseMode(ParseMode.HTML);
        message.setText(messageText);
        message.setReplyMarkup(goBackKeyboard());

        whatWant.merge(chatId, whatIs, (x,y) -> y);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage() + " (ошибка при отправке сообщения)");
        }
    }
    private void sendMessage(long chatId, String textToSend) { //отправка сообщения
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setParseMode(ParseMode.HTML);
        message.setText(textToSend);
        message.setReplyMarkup(standardKeyboard());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: " + e.getMessage() + " (ошибка при отправке сообщения)");
        }
    }

    // Практичней было бы сделать отдельного админского бота, через который уже бы и происходила работа с идеями и заявками
    // Но, опять же, для моих целей этого было достаточно.
    private void sendToAdmins(Update update, String whatIs) { //отрпавляет сообщение пользователя администрации
        for(long id : getAdmins()) {
            SendMessage message = new SendMessage();
            message.setChatId(id);
            message.setText(userMessageGenerator(update, whatIs));
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error: " + e.getMessage() + " (ошибка при отправке сообщения)");
            }
        }
        whatWant.put(update.getMessage().getChatId(), "");
    }

    private String userMessageGenerator(Update update, String whatIs) { //создает красивое письмицо, которое будет отправленно администрации
        StringBuilder sb = new StringBuilder();
        sb.append(whatIs + update.getMessage().getChat().getUserName() + " !\n\n");
        sb.append("\"" + update.getMessage().getText() + "\"");
        return sb.toString();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    public int[] getAdmins() {
        return config.getAdmins();
    }

}
