package be.roots.taconic.pricingguide.util;

public class SpecialCharactersUtil {

    public static String encode(String string) {
        return string
                .replaceAll("&reg;", "\u00AE")
                .replaceAll("&trade;", "\u2122")
                .replaceAll("&#39;", "'")
                .replaceAll("&ldquo;", "\"")
                .replaceAll("&rdquo;", "\"")
                ;
    }
}
