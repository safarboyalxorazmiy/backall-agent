package uz.backall.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ApiService {
  public Integer registerUser(String name, String surname, String magazineId, String password) {
    OkHttpClient client = new OkHttpClient();

    HttpUrl.Builder urlBuilder = HttpUrl.parse("http://backall.uz/api/v1/auth/register").newBuilder();
    String url = urlBuilder.build().toString();

    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String jsonPayload = objectMapper.writeValueAsString(new UserRegisterDTO(name, surname, magazineId, magazineId + "@backall.uz", password));

      RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonPayload);

      Request request = new Request.Builder()
        .url(url)
        .post(body)
        .addHeader("Content-Type", "application/json")
        .build();

      try (Response response = client.newCall(request).execute()) {
        return response.code();
      } catch (IOException e) {
        e.printStackTrace();
        return 403;
      }
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      e.printStackTrace();

      return 403;
    }
  }

}
