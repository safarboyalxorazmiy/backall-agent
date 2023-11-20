package uz.backall.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserRegisterDTO {
  private String firstname;
  private String lastname;
  private String storeName;
  private String email;
  private String password;
}
