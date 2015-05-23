package services.cls.parser;


import services.cls.parser.util.Node;
import services.cls.parser.util.NodeType;
import services.cls.parser.util.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Extrem einfache Hilfsklasse, die die rekursive Evaluation eines
 * ASTs ermöglicht. Voraussetzung für die Verwendung dieser Klasse
 * ist, das sich alle Werte auf einen gemeinsamen Typ <code>T</code>
 * zurückführen lassen (z.B. <code>Object</code> oder <code>Number</code>).
 */
public abstract class Evaluator<T> {

    /**
     * Evaluiert den AST (gegeben durch den <code>ASTNode node</code>)
     * rekursiv. Die tiefsten Elemente werden dabei zuerst evaluiert.
     * @param node Der AST
     * @return Evaluationsergebnis vom Typ <code>T</code>
     */
    public T evaluate(ASTNode node) throws EvaluatorException {
        if(node.getValue().getType() == NodeType.Expression) {
            return evaluateExpression(node.getValue());
        }

        List<T> arguments = new ArrayList<>();
        for(ASTNode arg : node.getArguments()) {
            arguments.add(evaluate(arg));
        }

        return evaluateOperation(node.getValue(), arguments);
    }

    /**
     * Gibt einen Wert ("12", "KonzeptXY") als <code>T</code> zurück,
     * d.h. führt im einfachsten Fall ein casting <code>String</code> nach
     * <code>T</code> durch.
     * @param node Der Knoten, dessen Wert zurück gegeben werden soll.
     * @return Der Wert des Knotens vom Typ <code>T</code>
     */
    public abstract T evaluateExpression(Node node) throws EvaluatorException;

    /**
     * Evaluiert eine Operation, wie z.B. "+" "und" "oder" mit einer
     * Liste von Argumenten vom Typ <code>T</code>
     * @param operator Knoten des Operators
     * @param arguments Liste der Argumente des Ausdrucks
     * @return Ein Wert vom Typ <code>T</code>
     */
    public abstract T evaluateOperation(Node operator, List<T> arguments) throws EvaluatorException;
}
