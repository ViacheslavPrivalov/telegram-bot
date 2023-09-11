package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.timer.BotTimer;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;

    private final NotificationTaskRepository repository;

    private final BotTimer timer;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskRepository repository, BotTimer timer) {
        this.telegramBot = telegramBot;
        this.repository = repository;
        this.timer = timer;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            // Process your updates here
            String startMessage = update.message().text();
            long chatId = update.message().chat().id();
            if (startMessage.equals("/start")) {
                telegramBot.execute(new SendMessage(chatId, "Hello!"));
            } else {
                saveTask(update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    public String[] parseMessage(Update update) {

        String userMessage = update.message().text();

        Pattern pattern = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)([\\W+]+)");
        Matcher matcher = pattern.matcher(userMessage);
        String str1 = "";
        String str2 = "";
        String[] strings = new String[2];

        if (matcher.matches()) {
            str1 = matcher.group(1);
            str2 = matcher.group(3);
        }

        strings[0] = str1;
        strings[1] = str2;

        return strings;
    }

    public LocalDateTime getDateTime(String[] strings) {

        CharSequence ch = new StringBuilder(strings[0]);

        return LocalDateTime.parse(ch, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public void saveTask(Update update) {
        String[] strings = parseMessage(update);

        LocalDateTime dateTime = getDateTime(strings);

        String message = strings[1];

        NotificationTask task = new NotificationTask();
        task.setUserId(update.message().chat().id());
        task.setDate(dateTime);
        task.setText(message);

        repository.save(task);

        telegramBot.execute(new SendMessage(update.message().chat().id(), "Task is added."));
    }


    @Scheduled(cron = "0 0/1 * * * *")
    public void checkTasks() {

        timer.check();
    }

}
