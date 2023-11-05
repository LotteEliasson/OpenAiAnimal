package lfe.openaianimal.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
//See here for a decent explanation of the parameters send to the API via the requestBody
//https://platform.openai.com/docs/api-reference/completions/create
@Getter
@Setter
public class ChatResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    @Getter
    @Setter
    public static class Choice {
        private int index;
        private Message message;
        private String finish_reason;
    }

    @Getter
    @Setter
    public static class Message {
        private String role;
        private String content;
    }

    @Getter
    @Setter
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }
}
