package com.example.quiz2;

import java.util.List;
public class Question {
    private int id;
    private String type;
    private String question;
    private boolean answerBoolean; // Para verdadero/falso
    private String answerString;   // Para opción múltiple
    private String explanation;
    private List<String> options;  // Para opción múltiple

    public Question(int id, String type, String question, boolean answerBoolean, String answerString, String explanation, List<String> options) {
        this.id = id;
        this.type = type;
        this.question = question;
        this.answerBoolean = answerBoolean;
        this.answerString = answerString;
        this.explanation = explanation;
        this.options = options;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getQuestion() {
        return question;
    }

    public boolean getAnswerBoolean() {
        return answerBoolean;
    }

    public String getAnswerString() {
        return answerString;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<String> getOptions() {
        return options;
    }
}
