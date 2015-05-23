package services.cls.parser;


import services.cls.parser.util.Node;

import java.util.ArrayList;

/**
 * Created by daniel on 12.05.15.
 */
public class Tokenizer {

    /**
     * Spaltet einen String in ein Array von String-Tokens auf
     * @param str der zu spaltende String
     * @return Ein Array von String-Tokens
     */
    public String[] tokenize(String str) {
        if(str.length() == 0) {
            return new String[0];
        }

        ArrayList<String> tokens = new ArrayList<>();

        int counter = 0;
        String token = "";
        do {
            char cur = str.charAt(counter);

            if(cur == Node.Identifier_Lparen.charAt(0)) {
                if(token.length() > 0) {
                    sanitizeAndAdd(token, tokens);
                    token = "";
                }

                tokens.add(Node.Identifier_Lparen);
                continue;
            }

            if(cur == Node.Identifier_Rparen.charAt(0)) {
                if(token.length() > 0) {
                    sanitizeAndAdd(token, tokens);
                    token = "";
                }

                tokens.add(Node.Identifier_Rparen);
                continue;
            }

            if(cur == ' ' || cur == '\n' || cur == '\r' || cur == '\t') {
                if(token.length() > 0) {
                    sanitizeAndAdd(token, tokens);
                    token = "";
                }
                continue;
            }

            token += cur;

        } while(++counter < str.length());

        sanitizeAndAdd(token, tokens);

        return tokens.toArray(new String[tokens.size()]);
    }

    /**
     * Bereinigt ein String-Token von überflüssigen Leerzeichen
     * @param token Ein Token, das bereinigt werden soll
     * @return das bereinigte Token
     */
    private String sanitzieToken(String token) {
        return token.trim();
    }

    /**
     * Bereinigt ein String-Token und fügt dieses, sollte es nach dem
     * Bereinigen eine Länge > 0 haben, der Liste <code>tokens</code>
     * hinzu.
     * @param token Das zu bereinigende und zu überprüfende Token
     * @param tokens Die ergebnisliste, in die das Token ggf. eingetragen wurde.
     */
    private void sanitizeAndAdd(String token, ArrayList<String> tokens) {
        if(token.length() == 0) {
            return;
        }

        String sanitized = sanitzieToken(token);

        if(sanitized.length() > 0) {
            tokens.add(sanitized);
        }
    }
}
