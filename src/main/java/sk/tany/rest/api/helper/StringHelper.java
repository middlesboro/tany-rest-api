package sk.tany.rest.api.helper;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringHelper {

    public static String slugify(String input) {
        if (input == null) {
            return null;
        }

        String nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String withoutDiacritics = pattern.matcher(nfdNormalizedString).replaceAll("");

        return withoutDiacritics.toLowerCase()
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }

}
