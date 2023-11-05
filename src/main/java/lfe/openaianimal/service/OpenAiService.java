package lfe.openaianimal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lfe.openaianimal.dto.ChatRequest;
import lfe.openaianimal.dto.ChatResponse;
import lfe.openaianimal.dto.MyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import javax.swing.*;
import java.net.URI;

/*
This code utilizes WebClient along with several other classes from org.springframework.web.reactive.
However, the code is NOT reactive due to the use of the block() method, which bridges the reactive code (WebClient)
to our imperative code (the way we have used Spring Boot up until now).

You will not truly benefit from WebClient unless you need to make several external requests in parallel.
Additionally, the WebClient API is very clean, so if you are familiar with HTTP, it should be easy to
understand what's going on in this code.
*/
@Service
public class OpenAiService {
    /* LoggerFactory.getLogger(OpenAiService.class): This is the initialization of the logger variable.
    It uses the LoggerFactory class to create (or retrieve, if it already exists) a logger instance
    associated with the OpenAiService class. This logger can then be used throughout the
    OpenAiService class (or other classes, since it's public) to log messages, such as debug messages,
    warnings, errors, etc.

    In simpler terms, this code is setting up a logging mechanism for the OpenAiService class,
    allowing it to log messages for debugging or monitoring purposes. */
    public static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);

    @Value("${API_KEY}")
    private String API_KEY;

    //See here for a decent explanation of the parameters send to the API via the requestBody
    //https://platform.openai.com/docs/api-reference/completions/create

    //parametre her under er def i endpoint the completion object i chatGPT, se link ovenover.
    public final static String URL = "https://api.openai.com/v1/chat/completions";
    public final static String MODEL = "gpt-4.0";
    public final static double TEMPERATURE = 0.8;
    public final static int MAX_TOKEN = 300;
    public final static double FREQUENCY_PENALTY = 0.0;
    public final static double PRESENCE_PENALTY = 0.0;
    public final static double TOP_P = 1.0;

//    WebClient: This is the type of the variable. WebClient is often used in Java,
//    especially with the Spring WebFlux framework, to make asynchronous web requests.
//    it's a non-blocking, reactive web client to perform HTTP requests.
    private WebClient client;

    public OpenAiService() {
        this.client = WebClient.create();
    }

// ????
    //Use this constructor for testing, to inject a mock client
    public OpenAiService(WebClient client) {
        this.client = client;
    }

    public MyResponse makeRequest(String userRequest, String systemReturnMessage) {
        ChatRequest request = new ChatRequest();
        request.setModel(MODEL);
        request.setTemperature(TEMPERATURE);
        request.setMax_tokens(MAX_TOKEN);
        request.setTop_p(TOP_P);
        request.setFrequency_penalty(FREQUENCY_PENALTY);
        request.setPresence_penalty(PRESENCE_PENALTY);

//        referere til List<Messages> der indeholder beskeden relateret til chatRequest.
//        tilføjer det til Array af beskeder hhv for user og system.
//        message har en role for OpenAis API forstår samtalens historie og giver relevant information i henhold til samtalen.
        request.getMessages().add(new ChatRequest.Message("system", systemReturnMessage));
        request.getMessages().add(new ChatRequest.Message("user", userRequest));


//      ObjectMapper bruges til at convertere Java Objekter til JSON og omvendt
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        String err = null;
        try {
//Mapper Java object til JSON. writeValueAsString er en metode i ObjectMapper der tager et java Object
//som et argument og returnere en JSON String.
            json = mapper.writeValueAsString(request);
            System.out.println(json);
            ChatResponse response = client.post()
                    .uri(new URI(URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(json))
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .block();
            String responseMessage = response.getChoices().get(0).getMessage().getContent();

//            Anvendes til ekstra formål at beregne hvor mange tokens der bruges ved en forspørgsel samt response.
            int tokensUsed = response.getUsage().getTotal_tokens();
            System.out.print("Tokens used: " + tokensUsed);
            System.out.print(". Cost ($0.0015 / 1K tokens) : $" + String.format("%6f",(tokensUsed * 0.0015 / 1000)));
            System.out.println(". For 1$, this is the amount of similar requests you can make: " + Math.round(1/(tokensUsed * 0.0015 / 1000)));

//            returnerer svaret på en brugers forspørgsel.
            return new MyResponse(responseMessage);



//          fejlhåndtering:
        } catch (WebClientResponseException e) {
            //This is how you can get the status code and message reported back by the remote API
            logger.error("Error response status code: " + e.getRawStatusCode());
            logger.error("Error response body: " + e.getResponseBodyAsString());
            logger.error("WebClientResponseException", e);
            err = "Internal Server Error, due to a failed request to external service. You could try again" +
                    "( While you develop, make sure to consult the detailed error message on your backend)";
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, err);
        } catch (Exception e) {
            logger.error("Exception", e);
            err = "Internal Server Error - You could try again" +
                    "( While you develop, make sure to consult the detailed error message on your backend)";
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, err);
        }
    }
}
