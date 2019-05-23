package ru.textamalysis.va;

import java.io.File;
import java.io.IOException;

import static ru.textamalysis.va.Validator.validAbbreviations;

public class Runner {
    public static void main(String[] args) throws IOException {
//        for (File file : new File(args[0]).listFiles()) {
//            if (file.getName().contains("docx")) {
                Boolean errors = validAbbreviations(new File(args[0] + "/_ТЗ_доработка_АИС_МФЦ_СПО- отработано.docx"));
//                System.out.println(file.getName() + " : " + (errors.isEmpty() ? "good" : errors));
//            }
//        }
    }
}
