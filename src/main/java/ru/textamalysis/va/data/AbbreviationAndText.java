package ru.textamalysis.va.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbbreviationAndText {
    private List<String> abbreviation;
    private String paragraph;

    public AbbreviationAndText(String abbreviation, String paragraph) {
        Pattern pattern = Pattern.compile("([А-Я]{2,})");
        Matcher matcher = pattern.matcher(abbreviation);
        this.abbreviation = new ArrayList<>();
        while (matcher.find()) {
            this.abbreviation.add(matcher.group(0));
        }
        if (abbreviation.isEmpty()) {
            this.abbreviation = Collections.singletonList(abbreviation);
        }
        this.paragraph = paragraph;
    }

    public List<String> getAbbreviation() {
        return abbreviation;
    }

    public String getParagraph() {
        return paragraph;
    }
}
