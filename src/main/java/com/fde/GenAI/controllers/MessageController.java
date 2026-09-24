package com.fde.GenAI.controllers;

import com.fde.GenAI.entities.PromptRequest;
import com.fde.GenAI.services.MessageService;
import com.google.genai.gaos.models.interactions.Interaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class MessageController {

    @Autowired
    private MessageService ms;

    @GetMapping
    public String greet() {
        return "HI";
    }

    @PostMapping("/sdk")
    public ResponseEntity<String> sendMessageByGenAISdk(@RequestBody PromptRequest prompt) {
        return ms.sendBySdk(prompt);
    }

    @PostMapping("/rc")
    public String sendMessageByRestTemplate(@RequestBody PromptRequest prompt) {

       String output = ms.sendByRestTemplate(prompt);
       return output;

    }

    @PostMapping("/chat")
    public Flux<String> chat(@RequestBody String ticket) {
        return ms.chat(ticket);
    }
}
