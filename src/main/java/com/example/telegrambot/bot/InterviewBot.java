package com.example.telegrambot.bot;

import com.example.telegrambot.model.Question;
import com.example.telegrambot.service.QuestionService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class InterviewBot extends TelegramLongPollingBot {


    @Override
    public String getBotUsername() {
        return "YOUR_BOT_USERNAME"; // Replace with your bot's username
    }

    @Override
    public String getBotToken() {
        return "YOUR_BOT_TOKEN"; // Replace with your bot's token
    }

    private static List<String> QUESTIONS;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();
            if (text.equalsIgnoreCase("/start")) {
                handleStartCommand(chatId);
            } else if (text.equalsIgnoreCase("/add_question")) {
                if (isAdmin(chatId)) {
                    sendModuleSelectionMessage(chatId);
                } else {
                    sendResponse(chatId, "You are not an admin. This command only for admins");
                }
            } else if (selectedModule != null && selectedChatId != null) {
                if (pendingAnswer) {
                    // We are expecting an answer now
                    Integer res = QuestionService.addQuestion(selectedModule, pendingQuestion, text, chatId);
                    if (res > 0) {
                        sendResponse(chatId, "You have successfully added question");
                    }else {
                        sendResponse(chatId, "Something went wrong");
                    }
                    selectedModule = null;
                    selectedChatId = null;
                    pendingQuestion = null;
                    pendingAnswer = false;
                } else {
                    // We are expecting a question first
                    pendingQuestion = text;
                    pendingAnswer = true;
                    askForAnswer(chatId);
                }
            }
            else if (isModule(text)) {

                SendMessage sendMessage = sendQuestions(text);
                sendResponse(chatId, sendMessage);
                System.out.println("questions sent");
            }

        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            System.out.println(data);

            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());

            if (data.startsWith("question_")) {
                String answer = QuestionService.getAnswerById(Integer.parseInt(data.substring(9)));
                System.out.println("answer: " + answer);
                String answerText = answer == null ? "No answer found for this question." : answer;
                answerCallbackQuery.setText(answerText);
                answerCallbackQuery.setShowAlert(true); // Display the text as an alert
            } else if (data.startsWith("module_")) {
                String module = data.substring(7);
                if (isModule(module)) {
                    askForQuestion(chatId);
                    selectedModule = module;
                    selectedChatId = chatId;
                    pendingAnswer = false;
                } else {
                    answerCallbackQuery.setText("Module not found!");
                }

            } else {
                answerCallbackQuery.setText("Data not found!");
                answerCallbackQuery.setShowAlert(true); // Display the text as an alert
            }

            alertResponse(answerCallbackQuery);
        }

    }

    private void handleStartCommand(Long chatId) {
        long userId = chatId;
        String username = null;

        try {
            GetChat getChat = new GetChat(String.valueOf(userId));
            Chat chat = execute(getChat);
            username = chat.getUserName();
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        SendMessage sendMessage = getKeyboardButtonModules();
        sendResponse(chatId, String.format("Welcome @%s! We are happy you are using our interviewer bot.", username), sendMessage);
    }

    private boolean isAdmin(Long chatId) {
        return chatId == 1059249931;
    }


    private void askForQuestion(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Please enter the question you want to add:");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void askForAnswer(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Please enter the answer for the question:");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private SendMessage sendQuestions(String moduleName) {
        List<String> questions = QuestionService.getRandomQuestions(moduleName);
        QUESTIONS = questions;
        System.out.println("questions: " + QUESTIONS);
        StringBuilder questionsText = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            String question = questions.get(i);
            questionsText.append(i + 1).append(". ").append(question).append("?\n");
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = getInlineKeyboardMarkupForAnswers(questions);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(questionsText.toString());
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);


        return sendMessage;
    }

    private boolean isModule(String text) {
        if (QuestionService.getAllModuleNames().contains(text)) return true;
        return false;
    }

    private InlineKeyboardMarkup getInlineKeyboardMarkupForAnswers(List<String> questions) {
        Integer questionId;
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            if (i - 1 < questions.size()) {
                InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                inlineKeyboardButton.setText(String.valueOf(i));
                questionId = QuestionService.getIdByQuestion(questions.get(i - 1));
                inlineKeyboardButton.setCallbackData(String.format("question_%d", questionId));

                if (i <= 5) {
                    row1.add(inlineKeyboardButton);
                } else {
                    row2.add(inlineKeyboardButton);
                }
            }
        }

        rows.add(row1);
        rows.add(row2);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }


    private SendMessage getKeyboardButtonModules() {
        SendMessage sendMessage = new SendMessage();
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        KeyboardRow keyboardRow2 = new KeyboardRow();
        KeyboardRow keyboardRow3 = new KeyboardRow();
        for (int i = 0; i < QuestionService.getAllModuleNames().size(); i++) {
            String buttonText = QuestionService.getAllModuleNames().get(i);
            KeyboardButton keyboardButton = new KeyboardButton(buttonText);
            if (i<2){
                keyboardRow1.add(keyboardButton);
            }else if (i<4){
                keyboardRow2.add(keyboardButton);
            }else if (i<6){
                keyboardRow3.add(keyboardButton);
            }
        }
        keyboardRows.add(keyboardRow1);
        keyboardRows.add(keyboardRow2);
        keyboardRows.add(keyboardRow3);
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        return sendMessage;
    }


    private void sendModuleSelectionMessage(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Select the module you want to add a question to:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<String> modules = QuestionService.getAllModuleNames();
        for (String module : modules) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(module);
            button.setCallbackData("module_" + module);
            row.add(button);
            rows.add(row);
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void removeKeyboard(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();
        replyKeyboardRemove.setRemoveKeyboard(true);
        message.setReplyMarkup(replyKeyboardRemove);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(long chatId, String text, SendMessage message) {
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(long chatId, SendMessage message) {
        message.setChatId(String.valueOf(chatId));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void alertResponse(AnswerCallbackQuery answerCallbackQuery) {
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private static String selectedModule = null;
    private static Long selectedChatId = null;
    private static String pendingQuestion = null;
    private static boolean pendingAnswer = false;

    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new InterviewBot());
            System.out.println("Bot started successfully.");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
