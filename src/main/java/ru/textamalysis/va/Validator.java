package ru.textamalysis.va;

import org.apache.poi.xwpf.usermodel.*;
import ru.textamalysis.va.data.AbbreviationAndText;
import ru.textanalysis.tfwwt.jmorfsdk.JMorfSdk;
import ru.textanalysis.tfwwt.jmorfsdk.load.JMorfSdkLoad;
import ru.textanalysis.tfwwt.morphological.structures.storage.OmoFormList;
import ru.textanalysis.tfwwt.parser.string.Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.textanalysis.tfwwt.morphological.structures.grammeme.MorfologyParameters.Name.ABBREVIATION;
import static ru.textanalysis.tfwwt.morphological.structures.grammeme.MorfologyParameters.Name.IDENTIFIER;

public class Validator {
    private static XWPFStyles styles;
    private static JMorfSdk jMorfSdk = JMorfSdkLoad.loadFullLibrary();
    private static List<String> terms = Arrays.asList("СОКРЕЩЕНИЯ", "НАИМЕНОВАНИЯ", "ТЕРМИНОВ", "ТЕРМИНЫ");

    public static Boolean validAbbreviations(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {
            styles = document.getStyles();
            ListIterator<IBodyElement> iterator = document.getBodyElements().listIterator();
            List<AbbreviationAndText> abbreviation = getAbbreviations(iterator);
            List<XWPFParagraph> text = getAbbreviationInText(document.getParagraphs().listIterator());

            Set<String> validateGr = validateGr(text, abbreviation);
            Set<String> validateMorf = validateMorf(text, abbreviation);

            System.out.println("Graf: " + validateGr);
            System.out.println("Morf: " + validateMorf);
            return !validateGr.isEmpty() && !validateMorf.isEmpty();
        }
    }

    private static List<AbbreviationAndText> getAbbreviations(ListIterator<IBodyElement> iterator) {
        List<AbbreviationAndText> paragraphs = new ArrayList<>();
        while (iterator.hasNext()) {
            IBodyElement element = iterator.next();
            if (element instanceof XWPFParagraph) {
                XWPFParagraph prg = (XWPFParagraph) element;
                if (prg.getStyleID() != null) {
                    if (isStyleBy(styles, prg)) {
                        List<String> word = Arrays.asList(prg.getText().toUpperCase().split(" "));
                        if (terms.stream().anyMatch(word::contains)) {
                            while (iterator.hasNext()) {
                                element = iterator.next();
                                if (element instanceof XWPFParagraph) {
                                    prg = (XWPFParagraph) element;
                                    if (!isStyleBy(styles, prg)) {
                                        if (!prg.getText().trim().isEmpty()) {
                                            String abr = prg.getText().split(" ")[0].trim();
                                            paragraphs.add(new AbbreviationAndText(abr, prg.getText()));
                                        }
                                    } else {
                                        break;
                                    }
                                } else {
                                    XWPFTable table = (XWPFTable) element;
                                    for(XWPFTableRow row : table.getRows()) {
                                        paragraphs.add(new AbbreviationAndText(row.getCell(0).getText(), row.getCell(1).getText()));
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
        return paragraphs;
    }


    private static List<XWPFParagraph> getAbbreviationInText(ListIterator<XWPFParagraph> iterator) {
        List<XWPFParagraph> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    private static boolean isStyleBy(XWPFStyles styles, XWPFParagraph prg) {
        return prg.getStyleID()!= null && styles.getStyle(prg.getStyleID()).getName().equals("heading 1");
    }

    private static Set<String> validateGr(List<XWPFParagraph> text, List<AbbreviationAndText> abbreviation) {
        Set<String> error = new HashSet<>();

        List<String> abbrs = new ArrayList<>();
        abbreviation.forEach(abbr -> {
            abbrs.addAll(abbr.getAbbreviation());
        });
        for(XWPFParagraph paragraph : text) {
            for(String word : paragraph.getText().split(" ")){
                Pattern pattern = Pattern.compile("([А-Я]{2,})");
                Matcher matcher = pattern.matcher(word);
                while (matcher.find()) {
                    if (!abbrs.contains(matcher.group(0))) {
                        error.add(word);
                    }
                }
            }
        }
        return error;
    }

    private static Set<String> validateMorf(List<XWPFParagraph> text, List<AbbreviationAndText> abbreviation) {
        Set<String> error = new HashSet<>();

        List<String> abbrs = new ArrayList<>();
        abbreviation.forEach(abbr -> {
            abbrs.addAll(abbr.getAbbreviation());
        });
        for(XWPFParagraph paragraph : text) {
            if (!paragraph.getText().trim().isEmpty()) {
                try {
                    for (String word : Parser.parserBasicsPhase(paragraph.getText())) {
                        Pattern pattern = Pattern.compile("([А-Я]{2,})");
                        Matcher matcher = pattern.matcher(word);
                        while (matcher.find()) {
                            if (!abbrs.contains(matcher.group(0))) {
                                OmoFormList list = jMorfSdk.getAllCharacteristicsOfForm(word.toLowerCase());
                                if (list.isEmpty()
                                        || list.stream().anyMatch(form -> (form.getAllMorfCharacteristics() & IDENTIFIER) == ABBREVIATION)) {
                                    error.add(word);
                                }
                            }
                        }
                    }
                } catch (RuntimeException ex) {

                }
            }
        }
        return error;
    }

}
