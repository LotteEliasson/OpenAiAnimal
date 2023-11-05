package lfe.openaianimal.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

//Denne klasse er generet ud fra playground View Code i ChatGPT, copy/paste in i ChatGPT
//Derved har chat generet klassens indhold.
@Getter
@Setter
public class ChatRequest {

    private String model;
    private List<Message> messages;
    private double temperature;
    private int max_tokens;
    private double top_p;
    private double frequency_penalty;
    private double presence_penalty;

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
