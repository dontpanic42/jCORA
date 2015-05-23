package services.cls.parser.util;

/**
 * Ein <code>Node</code> repräsentiert ein Token mit Typ.
 * Z.B. würde der Node ("+", Operator) einen Operator
 * definieren, der durch den String "+" identifiziert wird.
 *
 * Der Wert "42" könnte z.B. durch den <code>Node</code>
 * (42, Expression) definiert werden.
 */
public class Node {

    public static final String Identifier_Lparen = "(";
    public static final String Identifier_Rparen = ")";

    private final String identifier;
    private final NodeType type;

    /**
     * Konstruktor. Konstruiert einen Knoten anhand des Identifiers
     * (String, der diesen Knoten repäsentiert) und des Typs (wie
     * z.B. Operator, Function, Expression etc.)
     * @param identifier String, der diesen Knoten identifiziert
     * @param type Typ dieses Knotens
     */
    public Node(String identifier, NodeType type) {
        this.identifier = identifier;
        this.type = type;
    }

    /**
     * Der String, der diesen Knoten repräsentiert, z.B. "+" oder "42"
     * @return Der String, der diesen Knoten repäsentiert
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Typ dieses Knotens, z.B. Operator, Function, Expression etc.
     * @return Typ dieses Knotens
     */
    public NodeType getType() {
        return type;
    }
}
