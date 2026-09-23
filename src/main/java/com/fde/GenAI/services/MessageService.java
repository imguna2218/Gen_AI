package com.fde.GenAI.services;

import com.fde.GenAI.entities.AIResponseEntity;
import com.fde.GenAI.entities.PromptRequest;
import com.fde.GenAI.entities.SDKResponseEntity;
import com.google.genai.Client;
import com.google.genai.gaos.models.interactions.*;
import com.google.genai.gaos.models.operations.CreateInteractionRequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.ai.chat.client.ChatClient;


import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageService {
    @Value("${google_api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ChatClient chatClient;

    public ResponseEntity<String> sendBySdk(PromptRequest prompt) {
        try {
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            CreateModelInteraction params =
                    CreateModelInteraction.builder()
                            .model(Model.of("gemini-2.5-flash"))
                            .input(InteractionsInput.of(prompt.getPrompt()))
                            .build();

            Interaction interaction =
                    client.interactions.create(
                            CreateInteractionRequestBody.of(params)
                    ).interaction().get();
            String text = "";
            for (Step step : interaction.steps().get()) {
                if (step instanceof ModelOutputStep outputStep) {
                    for (Content content : outputStep.content().get()) {
                        if (content instanceof TextContent textContent) {
                            text = textContent.text().orElse("");
                            System.out.println("TEXT : ");
                            System.out.println(text);
                        }
                    }
                }
            }

            System.out.println("INTERACTION:");
            System.out.println(interaction);

            System.out.println("OUTPUT TEXT:");
            System.out.println(interaction.outputText());

            return new ResponseEntity<>(
                    text,
                    HttpStatus.OK
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(
                    "An Error occured while sending to LLM : " + e.getMessage(),
                    HttpStatus.CONFLICT
            );
        }
    }


    public String sendByRestTemplate(PromptRequest prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/interactions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-goog-api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("model", "gemini-2.5-flash");
        body.put("input", prompt.getPrompt());

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<AIResponseEntity> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        AIResponseEntity.class
                );

        return response.getBody().steps.stream()
                .filter(step -> "model_output".equals(step.type))
                .findFirst()
                .get()
                .content.getFirst()
                .text;
    }

    public String summarize(String ticket) {
        String output = chatClient.prompt()
                .user("Summarise this support ticket in two lines "+"\n\n"+ticket)
                .call()
                .content();

        return output;
    }
}