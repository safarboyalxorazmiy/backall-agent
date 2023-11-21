package uz.backall.user.history;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserHistoryService {
  @Autowired
  private UserHistoryRepository userHistoryRepository;

  // TODO Foydalanuvchining oxirgi ochgan labelini olish
  public Label getLastLabelByChatId(Long chatId) {
    Optional<UserHistoryEntity> last = userHistoryRepository.getLast(chatId);
    if (last.isPresent()) {
      return last.get().getLabel();
    }

    return null;
  }

  public String getLastValueByChatId(Long chatId, Label label) {
    Optional<UserHistoryEntity> last = userHistoryRepository.getLastByLabel(label.name(), chatId);
    return last.map(UserHistoryEntity::getValue).orElse(null);
  }

  public void create(Label label, long chatId, String value) {
    UserHistoryEntity entity = new UserHistoryEntity();
    entity.setLabel(label);
    entity.setUserId(chatId);
    entity.setValue(value);
    userHistoryRepository.save(entity);
  }

  public void clearHistory(Long chatId) {
    userHistoryRepository.deleteByUserId(chatId);
  }
}