package org.streamreasoning.sld.processing.jena.syntax;


public class RDFUtils {

    public static String trimTags(String s) {
        return s.replaceAll("^<(.*)>$", "$1");
    }

    public static String trimQuotes(String s) {
        return s.replaceAll("^['\"](.*)['\"]$", "$1");
    }

    public static String trimFirst(String s) {
        return s.replaceAll("^.(.*)$", "$1");
    }

    public static String trimLast(String s) {
        return s.replaceAll("^(.*).$", "$1");
    }


    public static String trimVar(String inputVariable) {
        return inputVariable.startsWith("?") ? trimFirst(inputVariable) : inputVariable;
    }

    public static double parseDouble(String doubleValue) {
        return Double.parseDouble(removeDataType(doubleValue));
    }

    private static String removeDataType(String dataLiteral) {
        return dataLiteral.substring(1, dataLiteral.lastIndexOf('"'));
    }
}
