package services.cls.parser.util.ast;

import services.cls.parser.util.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Ein Knoten des Abstract Syntax Trees. Einthält jeweils einen Wert
 * (der ein Operator, eine Funktion oder eine Expression enthält) und
 * eine Liste von Argumenten (wenn Operator oder Funktion).
 */
public class ASTNode {
    private Node op;
    private List<ASTNode> arguments = new ArrayList<>();

    /**
     * Konstruktor
     * @param op der Wert des Knotens
     */
    public ASTNode(Node op) {
        this.op = op;
    }

    /**
     * Wert dieses Knoten
     * @return
     */
    public Node getValue() {
        return op;
    }

    /**
     * Argumente für den Operator/die Funktion. A.k.a. Kinder dieses Knotens
     * @return
     */
    public List<ASTNode> getArguments() {
        return arguments;
    }
}
