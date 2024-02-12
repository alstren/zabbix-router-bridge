package ru.krsmon.zabbixrouterbridge.external.telegram.bots;

import static ru.krsmon.zabbixrouterbridge.config.SecurityConfig.ROLE_ROOT;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.krsmon.zabbixrouterbridge.domain.model.User;
import ru.krsmon.zabbixrouterbridge.domain.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BridgeNotificationBot extends TelegramLongPollingBot {
  private final UserService userService;

  @Value("${external.bots.bridgeNotifyBot.botUsername}")
  protected String botUsername;

  @Value("${external.bots.bridgeNotifyBot.botToken}")
  protected String botToken;

  @Override
  public void onUpdateReceived(@NonNull Update update) {
    if (update.hasMessage() && update.getMessage().hasText() && validateUser(update.getMessage())) {
      log.info("NOTIFY: Received сmd '%s' from user '%s'"
          .formatted(update.getMessage().getText(), update.getMessage().getFrom().getId()));

      // TODO: work with requested command...
    }
  }

  private boolean validateUser(Message message) {
    log.info("BRIDGE: received message '%s' from '%s'.".formatted(message.getText(), message.getFrom().getId()));
    if (message.getFrom().getIsBot()) return false;
    var user = userService.findUserByTelegramId(message.getFrom().getId().toString());

    if (user.isEmpty() || !isAdmin(user.get())) {
      sendMessage(message, "Access denied.");
      log.warn("BRIDGE: '%s' is not admin, ignoring message.".formatted(message.getFrom().getId()));
      return false;
    }
    return true;
  }

  @SneakyThrows
  private void sendMessage(@NonNull Message source, @NonNull String response) {
    var newMessage = new SendMessage();
    newMessage.setChatId(source.getChatId());
    newMessage.setReplyToMessageId(source.getMessageId());
    newMessage.setText(response);
    executeAsync(newMessage);
  }

  private boolean isAdmin(User user) {
    return user.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equalsIgnoreCase(ROLE_ROOT));
  }

  @SneakyThrows
  public void notifyAdmin(@NonNull String text) {
    log.info("BRIDGE: Inform admins, message '%s'".formatted(text));
    for (var id : userService.findAll().stream().filter(this::isAdmin).map(User::getTelegramId).toList()) {
      var message = new SendMessage();
      message.setChatId(id);
      message.setText(text);
      executeAsync(message);
    }
  }

  @Override
  public String getBotUsername() {
    return botUsername;
  }

  @Override
  public String getBotToken() {
    return botToken;
  }

}
