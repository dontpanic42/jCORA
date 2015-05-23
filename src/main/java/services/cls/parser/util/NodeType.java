package services.cls.parser.util;

/**
 * Jedes vom Parser betrachtete Token lässt sich einer der folgenden Kategorien
 * zuordnen:
 *
 * * Expression: Ein "Wert", z.B. "12" oder "Auto", alles was weder Funktion, Klammer, ArgumentSeperator oder Operator ist.
 * * Function: Eine Funktion
 * * ArgumentSeperator: Ein element, das die Argumente einer Funktion separiert (",")
 * * Operator: Ein Infix-Operator (z.B. "+", "-")
 * * Lparen: Eine öffnende Klammer ("(")
 * * Rparen: Eine schließende Klammer (")")
 */
public enum NodeType {
    Expression,
    Function,
    ArgumentSeperator,
    Operator,
    Lparen,
    Rparen
}
