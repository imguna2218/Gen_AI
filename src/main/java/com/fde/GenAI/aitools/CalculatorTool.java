package com.fde.GenAI.aitools;

import org.intellij.lang.annotations.JdkConstants;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTool {

    @Tool(description = """
            Performs arithmetic calculations.
            Supportted operations are : add, substract, divide, multiply, power, mod
            """)
    public double calculate(
            @ToolParam(description = "Operation : add, substract, divide, multiply, power, mod")
            String operation,

            @ToolParam(description = "First number")
            double a,

            @ToolParam(description = "Second number")
            double b) {

        System.out.println("Calculator tool called");
        if(operation.equals("add")) {
            return a + b;
        } else if(operation.equals("substract")) {
            return a - b;
        } else if(operation.equals("divide")) {
            return a / b;
        } else if (operation.equals("multiply")) {
            return a * b;
        } else if(operation.equals("power")) {
            return Math.pow(a, b);
        } else if(operation.equals("mod")){
          return a % b;
        } else {
            throw new IllegalArgumentException("Unsupported exception : " + operation);
        }
    }
}
