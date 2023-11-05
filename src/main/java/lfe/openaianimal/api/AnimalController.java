package lfe.openaianimal.api;

import lfe.openaianimal.dto.MyResponse;
import lfe.openaianimal.service.OpenAiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/animal")
@CrossOrigin(origins = "*")
public class AnimalController {
    private OpenAiService service;

    final static String SYSTEM_MESSAGE = "You are a helpful assistant, that answer in danish about animals in a given country." +
            "The users are primarily thought of as children asking about which animals that lives in a country the are interested in" +
            "therefore the answer should be readable for children and information not extremely theoretical" +
            "Might be nice if you could add some fun facts about the animals you provide from the given country.";

    public AnimalController(OpenAiService service){
        this.service = service;
    }

    @GetMapping
    public MyResponse getAnimal(@RequestParam String about){
        return service.makeRequest(about, SYSTEM_MESSAGE);
    }
}
