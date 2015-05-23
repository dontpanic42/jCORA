package services.cls.parser.util.nodes;

import services.cls.parser.util.Node;
import services.cls.parser.util.NodeType;

/**
 * Knoten, der einen Operator repräsentiert
 */
public class Operator extends Node {
    private final int precedence;
    private final boolean isLeftAssociative;

    /**
     * Konstruktor
     * @param identifier String, der diesen Knoten identifiziert
     * @param precedence Präzedenz - je höher, desto "wichtiger" der operator. Z.B. "+": 1, "*": 2
     * @param isLeftAssociative Linksassoziativ ja/nein
     */
    public Operator(String identifier, int precedence, boolean isLeftAssociative) {
        super(identifier, NodeType.Operator);
        this.precedence = precedence;
        this.isLeftAssociative = isLeftAssociative;
    }

    /**
     * Gibt die Präzedenz des Operators zurück
     * @return
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * Gibt zurück, ob dieser Operator links-assoziativ ist.
     * @return
     */
    public boolean getIsLeftAssociative() {
        return isLeftAssociative;
    }
}
