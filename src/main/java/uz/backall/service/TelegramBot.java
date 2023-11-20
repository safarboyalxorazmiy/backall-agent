package uz.backall.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.backall.api.ApiService;
import uz.backall.config.BotConfig;
import uz.backall.user.Role;
import uz.backall.user.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.backall.user.history.Label;
import uz.backall.user.history.UserHistoryService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final UsersService usersService;
    private final UserHistoryService userHistoryService;

    private final ApiService apiService;

    public TelegramBot(BotConfig config, UsersService usersService, UserHistoryService userHistoryService, ApiService apiService) {
        this.config = config;
        this.usersService = usersService;
        this.userHistoryService = userHistoryService;
        this.apiService = apiService;

        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Boshlash"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error during setting bot's command list: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            long chatId = update.getMessage().getChatId();
            if (update.getMessage().getChat().getType().equals("supergroup")) {
                // DO NOTHING CHANNEL CHAT ID IS -1001764816733
                return;
            } else {
                Role role = usersService.getRoleByChatId(chatId);

                if (update.hasMessage() && update.getMessage().hasText()) {
                    String messageText = update.getMessage().getText();

                    if (messageText.startsWith("/")) {
                        if (messageText.startsWith("/login ")) {
                            String password = messageText.substring(7);

                            if (password.equals("Xp2s5v8y/B?E(H+KbPeShVmYq3t6w9z$C&F)J@NcQfTjWnZr4u7x!A%D*G-KaPdSgUkXp2s5v8y/B?E(H+MbQeThWmYq3t6w9z$C&F)J@NcRfUjXn2r4u7x!A%D*G-Ka")) {
                                usersService.changeRole(chatId, Role.ROLE_AGENT);
                                startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());
                                return;
                            }
                            return;
                        }

                        switch (messageText) {
                            case "/start" -> {
                                startCommandReceived(chatId, update.getMessage().getChat().getFirstName(), update.getMessage().getChat().getLastName());
                                return;
                            }
                            case "/help" -> {
                                helpCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                                return;
                            }
                            default -> {
                                sendMessage(chatId, "Sorry, command was not recognized");
                                return;
                            }
                        }
                    }

                    if (role.equals(Role.ROLE_AGENT)) {
                        if (messageText.equals("Foydalanuvchi qo'shish ➕")) {
                            sendMessageWithKeyboardButton(chatId, "Foydalanuvchining ismini kiriting.", "Bekor qilish \uD83D\uDD19");

                            userHistoryService.clearHistory(chatId);
                            userHistoryService.create(Label.OFFER_STARTED, chatId, "NO_VALUE");
                        }
                        else if (messageText.equals("Bekor qilish \uD83D\uDD19")) {
                            sendMessageWithKeyboardButton(chatId, "Bosh menyu \uD83C\uDFD8", "Foydalanuvchi qo'shish ➕");
                            userHistoryService.clearHistory(chatId);
                        }
                        else if (messageText.equals("Batafsil ma'lumot olish \uD83D\uDCDD")) {
                            sendMessageWithKeyboardButton(chatId, "<b>Telegram bot bilan bog'liq qandaydur muammo kuzatilsa asoschi Safarboy bilan bog'laning!</b> \n" +
                              "\uD83D\uDCF2 +998917972385  -  @dasturchialxorazmiy", "Sotuvni davom ettirish \uD83D\uDED2");
                        }
                        else if (messageText.equals("Sotuvni davom ettirish \uD83D\uDED2")) {
                            sendMessageWithKeyboardButton(chatId, "Bosh menyu \uD83C\uDFD8", "Foydalanuvchi qo'shish ➕");
                            userHistoryService.clearHistory(chatId);
                        }
                        else if (messageText.equals("Bosh menyuga qaytish \uD83D\uDD19")) {
                            sendMessageWithKeyboardButton(chatId, "Bosh menyu \uD83C\uDFD8", "Foydalanuvchi qo'shish ➕");
                            userHistoryService.clearHistory(chatId);
                        }
                        else {
                            Label lastLabelByChatId = userHistoryService.getLastLabelByChatId(chatId);

                            if (lastLabelByChatId != null) {
                                if (lastLabelByChatId.equals(Label.OFFER_STARTED)) {
                                    userHistoryService.create(Label.NAME_ENTERED, chatId, messageText);
                                    sendMessageWithKeyboardButton(chatId, "Foydalanuvchining familyasini kiriting.", "Bekor qilish \uD83D\uDD19");
                                }
                                else if (lastLabelByChatId.equals(Label.NAME_ENTERED)) {
                                    userHistoryService.create(Label.SURNAME_ENTERED, chatId, messageText);
                                    sendMessageWithKeyboardButton(chatId, "Magazin INNsini kiriting..", "Bekor qilish \uD83D\uDD19");
                                }
                                else if (lastLabelByChatId.equals(Label.SURNAME_ENTERED)) {
                                    userHistoryService.create(Label.ID_ENETERED, chatId, messageText);
                                    sendMessageWithKeyboardButton(chatId, "Parolni kiriting..", "Bekor qilish \uD83D\uDD19");
                                }
                                else if (lastLabelByChatId.equals(Label.ID_ENETERED)) {
                                    String name = userHistoryService.getLastValueByChatId(chatId, Label.NAME_ENTERED);
                                    String surname = userHistoryService.getLastValueByChatId(chatId, Label.SURNAME_ENTERED);
                                    String magazineId = userHistoryService.getLastValueByChatId(chatId, Label.ID_ENETERED);
                                    String password = messageText;

                                    Integer isRegistered = apiService.registerUser(name, surname, magazineId, password);
                                    if (isRegistered.equals(200)) {
                                        sendMessageWithKeyboardButton(chatId, "Ajoyib, yaratildi. ✅", "Foydalanuvchi qo'shish ➕");
                                    } else if (isRegistered.equals(400)) {
                                        sendMessageWithKeyboardButton(chatId, "Bu INN allaqachon ro'yxatdan o'tgan.", "Bosh menyuga qaytish \uD83D\uDD19");
                                    } else {
                                        sendMessageWithKeyboardButton(chatId, "Server ishlamayapti, Iltimos hoziroq adminlar bilan bog'laning!", "Batafsil ma'lumot olish \uD83D\uDCDD");
                                    }

                                    userHistoryService.clearHistory(chatId);
                                }
                            }
                            else {
                                sendMessageWithKeyboardButton(chatId, "Siz bosh menyudasiz \uD83C\uDFD8 \n" +
                                  "Pastdagi amallardan birini tanlang \uD83D\uDC47", "Foydalanuvchi qo'shish ➕");
                            }
                        }
                    } else if (role.equals(Role.ROLE_USER)) {
                    }
                }
                if (update.hasMessage() && update.getMessage().hasPhoto()) {

                }
            }

        }
    }

    private void startCommandReceived(long chatId, String firstName, String lastName) {
        Role role = usersService.createUser(chatId, firstName, lastName).getRole();

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.enableHtml(true);

        if (role.equals(Role.ROLE_USER)) {
            message.setText("Welcome User, What's up?");
        } else if (role.equals(Role.ROLE_AGENT)) {
            message.setText("Hush kelibsiz, Agent!");

            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> rows = new ArrayList<>();
            KeyboardRow row = new KeyboardRow();
            row.add("Foydalanuvchi qo'shish ➕");
            rows.add(row);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setKeyboard(rows);

            message.setReplyMarkup(replyKeyboardMarkup);
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error in startCommandReceived()");
        }
    }

    private void helpCommandReceived(long chatId, String firstName) {}

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText(textToSend);
        message.enableHtml(true);
        try {
            execute(message);
        } catch (TelegramApiException ignored) {
            log.error("Error in sendMessage()");
        }
    }

    private void sendMessageWithKeyboardButton(long chatId, String textToSend, String keyboardRowText) {
        SendMessage message = new SendMessage();

        message.setChatId(chatId);
        message.setText(textToSend);
        message.enableHtml(true);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(keyboardRowText);
        rows.add(row);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);

        message.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException ignored) {
            log.error("Error in sendMessageWithKeyboardButton()");
        }
    }
}