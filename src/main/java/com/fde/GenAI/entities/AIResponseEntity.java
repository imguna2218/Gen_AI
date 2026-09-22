package com.fde.GenAI.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class AIResponseEntity {

    public ArrayList<Step> steps;

    @Getter
    @Setter
    public static class Step {
        public String type;
        public ArrayList<Content> content;
    }

    @Getter
    @Setter
    public static class Content {
        public String text;
        public String type;
    }
}
