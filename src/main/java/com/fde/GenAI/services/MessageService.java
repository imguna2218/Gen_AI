package com.fde.GenAI.services;

import com.fde.GenAI.aitools.CalculatorTool;
import com.fde.GenAI.entities.AIResponseEntity;
import com.fde.GenAI.entities.PromptRequest;
import com.fde.GenAI.entities.SDKResponseEntity;
import com.google.genai.Client;
import com.google.genai.gaos.models.interactions.*;
import com.google.genai.gaos.models.operations.CreateInteractionRequestBody;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.ai.chat.client.ChatClient;


import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {
    @Value("${google_api.key}")
    private String apiKey;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private CalculatorTool calculatorTool;

    private List<Message> history = new ArrayList<>();

    private final String systemPrompt = """
                You are a helpful AI assistant with access to external tools.

                Follow these rules:
                1. For Arithmetic calculations, ALWAYS use the calculator tool.
                2. After recieving tool results , respond to the user in Natural language.
    """;


    public ResponseEntity<String> sendBySdk(PromptRequest prompt) {
        try {
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            CreateModelInteraction params =
                    CreateModelInteraction.builder()
                            .model(Model.of("gemini-3.1-flash-lite"))
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
        body.put("model", "gemini-3.1-flash-lite");
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

    public String chat(String message) {

        history.add(new UserMessage(message));

        String output = chatClient.prompt()
                .system(systemPrompt)
                .messages(history)
                .tools(calculatorTool)
                .call()
                .content();

        history.add(new AssistantMessage(output));
        return output;
    }
}