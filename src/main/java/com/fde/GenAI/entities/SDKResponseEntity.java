package com.fde.GenAI.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class SDKResponseEntity {

    private Object agent;
    private Object agentConfig;
    private String created;
    private Object environment;
    private Object environmentId;
    private Object errors;
    private Object generationConfig;
    private String id;
    private Object input;
    private Object labels;
    private Model model;

    private Object outputAudio;
    private Object outputImage;
    private Object outputText;
    private Object outputVideo;

    private Object previousInteractionId;
    private Object responseFormat;
    private Object responseMimeType;
    private Object responseModalities;
    private Object safetySettings;
    private ServiceTier serviceTier;
    private String status;

    private ArrayList<Step> steps;

    private Object systemInstruction;
    private Object tools;
    private String updated;
    private Usage usage;
    private Object webhookConfig;


    @Getter
    @Setter
    public static class Model {
        private String value;
    }


    @Getter
    @Setter
    public static class ServiceTier {
        private String value;
    }


    @Getter
    @Setter
    public static class Step {

        private String type;

        private String signature;

        private String summary;

        private ArrayList<Content> content;

        private Object error;
    }


    @Getter
    @Setter
    public static class Content {

        private String type;

        private String text;

        private Object annotations;
    }


    @Getter
    @Setter
    public static class Usage {

        private Object cachedTokensByModality;

        private Object groundingToolCount;

        private ArrayList<ModalityTokens> inputTokensByModality;

        private ArrayList<ModalityTokens> outputTokensByModality;

        private ArrayList<ModalityTokens> toolUseTokensByModality;

        private int totalCachedTokens;

        private int totalInputTokens;

        private int totalOutputTokens;

        private int totalThoughtTokens;

        private int totalTokens;

        private int totalToolUseTokens;
    }


    @Getter
    @Setter
    public static class ModalityTokens {

        private Modality modality;

        private int tokens;
    }


    @Getter
    @Setter
    public static class Modality {

        private String value;
    }
}