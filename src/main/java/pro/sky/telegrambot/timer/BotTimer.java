package pro.sky.telegrambot.timer;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class BotTimer {
    private final TelegramBot telegramBot;

    private final NotificationTaskRepository repository;

    public BotTimer(TelegramBot telegramBot, NotificationTaskRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    public void check() {

        List<NotificationTask> tasks = repository.findAll();

        for (NotificationTask task : tasks) {
            if (task.getDate().equals(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES))) {
                telegramBot.execute(new SendMessage(task.getUserId(), task.getText()));
                repository.delete(task);
            }
        }
    }
}
