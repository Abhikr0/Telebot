package resourse;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private boolean screaming = false;

    private InlineKeyboardMarkup keyboardM1;
    private InlineKeyboardMarkup keyboardM2;

    @Override
    public String getBotUsername() {
        return "Resource Bot";
    }

    @Override
    public String getBotToken() {
        return "7834110493:AAH8GkkM9A5OtuzytGMYWmVbjnnrSXmVzbw" ;
    }

    @Override
    public void onUpdateReceived(Update update) {


//        User data
        var msg = update.getMessage();
        var user = msg.getFrom();
        var userId = user.getId();
        var txt = msg.getText();
//        keyboard buttons
        var next = InlineKeyboardButton.builder()
                .text("Next").callbackData("next")
                .build();

        var back = InlineKeyboardButton.builder()
                .text("Back").callbackData("back")
                .build();

        var url = InlineKeyboardButton.builder()
                .text("Resources")
                .url("https://core.telegram.org/bots/api")
                .build();
//        user keyboard
        keyboardM1 =InlineKeyboardMarkup.builder().keyboardRow(List.of(next)).build();

        keyboardM2 = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(back))
                .keyboardRow(List.of(url))
                .build();

        String welcome = "Hello!! Nice to meet you" +"\n"+
                "how can i help you ";

// conditions
        if(msg.isCommand()){

            if (txt.equals("/scream")){
                screaming = true;
            }
            else if (txt.equals("/whisper")) {
                screaming = false;
            }
            else if (txt.equals("/start")){
                sendText(userId,welcome);
            }
            else if (txt.equals("/menu")){
                sendMenu(userId,"<b>Menu 1</b>",keyboardM1);
            }
        }
        if(screaming){
            scream(userId,msg);
        }
        else {
            copyMessage(userId,msg.getMessageId());
        }



    }

    public void scream(Long id, Message msg){
        if(msg.hasText())
            sendText(id,msg.getText().toUpperCase());
        else
            copyMessage(id,msg.getMessageId());
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder().chatId(who.toString()).text(what).build();

        try{
            execute(sm);
        } catch (TelegramApiException e){
            throw new IllegalArgumentException(e);
        }
    }
    public void copyMessage(Long who, Integer msgId) {
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())  //We copy from the user
                .chatId(who.toString())      //And send it back to him
                .messageId(msgId)            //Specifying what message
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public void sendMenu(Long who, String txt, InlineKeyboardMarkup kb){
        SendMessage sm = SendMessage.builder().chatId(who.toString())
                .parseMode("HTML").text(txt)
                .replyMarkup(kb).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void buttonTap(Long id, String queryId, String data,int msgId) throws TelegramApiException {
        EditMessageText newTxt = EditMessageText.builder().chatId(id.toString()).messageId(msgId).text("").build();

        EditMessageReplyMarkup newKb = EditMessageReplyMarkup.builder().chatId(id.toString()).messageId(msgId).build();

        if(data.equals("next")) {
            newTxt.setText("MENU 2");
            newKb.setReplyMarkup(keyboardM2);
        } else if(data.equals("back")) {
            newTxt.setText("MENU 1");
            newKb.setReplyMarkup(keyboardM1);
        }

        AnswerCallbackQuery close = AnswerCallbackQuery.builder()
                .callbackQueryId(queryId).build();

        execute(close);
        execute(newTxt);
        execute(newKb);
    }


    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot();
        botsApi.registerBot(bot);
        bot.sendText(1234L, "Hello World!");

    }


}
