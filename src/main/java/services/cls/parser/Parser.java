package services.cls.parser;


import services.cls.parser.util.Node;
import services.cls.parser.util.NodeType;
import services.cls.parser.util.ast.ASTNode;
import services.cls.parser.util.nodes.Operator;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by daniel on 12.05.15.
 */
public class Parser {



    /**
     * Liste der bekannten Operatoren, Funktionen, Klammern etc.
     */
    private List<Node> buildins = new ArrayList<Node>() {
        {
            add(new Node(Node.Identifier_Lparen, NodeType.Lparen));
            add(new Node(Node.Identifier_Rparen, NodeType.Rparen));
            add(new Operator("und", 1, true));
            add(new Operator("oder", 1, true));
        }
    };

    /**
     * Gibt die Liste der dem Parser bekannten Operatoren, Funktionen etc. zurück.
     * @return Liste der bekannten nicht-Expressions
     */
    public List<Node> getBuildins() {
        return buildins;
    }

    /**
     * Fügt dem Parser eine Funktion, einen Operator etc. hinzu.
     * @param buildin Die nicht-Expression.
     */
    public void addBuildin(Node buildin) {
        buildins.add(buildin);
    }

    /**
     * Erwartet eine Liste von <code>Nodes</code>, die in RPN reihenfolge evaluiert werden kann.
     * Diese Liste wird geparst und als Baum (<code>AST</code>) zurückgegeben. Dieser Baum kann
     * dann rekursiv abgearbeitet werden, um den Wert des Ausdrucks zu bestimmen. Die tiefsten
     * Elemente müssen dabei zuerst evaluiert werden.
     * @param nodes Liste der Knoten in RPN
     * @return Ein Baum
     * @throws ParserException bei Syntaxfehlern
     */
    public ASTNode toAST(List<Node> nodes)
            throws ParserException {
        if(nodes.size() == 0) {
            return null;
        }

        if(nodes.size() == 1) {
            if(nodes.get(0).getType() == NodeType.Expression) {
                return new ASTNode(nodes.get(0));
            } else {
                throw new ParserException("Expecting expression");
            }
        }

        Deque<ASTNode> stack = new ArrayDeque<>();
        for(Node node : nodes) {
            if (node.getType() == NodeType.Expression) {
                stack.addFirst(new ASTNode(node));
            } else {
                /* Der Typ 'Operator' hat immer zwei argumente... */
                if (node.getType() == NodeType.Operator) {
                    if (stack.size() < 2) {
                        throw new ParserException("Expected at least 2 arguments for operator " +
                                node.getIdentifier());
                    }

                    ASTNode op = new ASTNode(node);
                    op.getArguments().add(stack.removeFirst());
                    op.getArguments().add(stack.removeFirst());

                    /* Die Argumente liegen derzeit "verkehrtherum" in der Liste */
                    Collections.reverse(op.getArguments());

                    stack.addFirst(op);
                } else {
                    /* Funktionen sind noch nicht implementiert... */
                    throw new NotImplementedException();
                }
            }
        }

        if(stack.size() > 1) {
            throw new ParserException("Syntax error (stack > 1)");
        }

        return stack.getFirst();
    }

    /**
     * Identifiziert Operatoren, Funktionen und Expressions (Werte) in
     * einer Liste von String tokens. Es wird davon ausgegangen, das
     * die Liste im klassischen "Infix" Format vorliegt.
     *
     * Sind alle Operatoren, Funktionen und Expressions identifiziert, wird
     * die Liste von String-Tokens in RPN umsortiert.
     * @param tokens Liste von String-Tokens in Infix-Reihenfolge
     * @return Liste von <code>Nodes</code> in RPN-Reihenfolge
     * @throws ParserException bei Syntaxfehlern
     */
    public List<Node> toRPN(String[] tokens)
            throws ParserException {
        Deque<Node> stack = new ArrayDeque<>();
        Queue<Node> queue = new LinkedList<>();

        Node node;

        for(int i = 0; i < tokens.length; i++) {
            node = getNodeForToken(tokens[i]);
            switch (node.getType()) {
                case Expression: {
                    queue.add(node);
                    break;
                }
                case Function: {
                    //Normalerweise: Auf den Stack...
                    throw new NotImplementedException();
                }
                case ArgumentSeperator: {
                    //Normalerweise: In schleife...
                    throw new NotImplementedException();
                }
                case Operator: {
                    Operator o1 = (Operator) node;

                    while (stack.size() > 0 &&
                            stack.getFirst().getType() == NodeType.Operator) {
                        Operator o2 = (Operator) stack.getFirst();

                        if ((o1.getIsLeftAssociative() &&
                                o1.getPrecedence() <= o2.getPrecedence()) ||
                                (!o1.getIsLeftAssociative() &&
                                        o1.getPrecedence() < o2.getPrecedence())) {

                            stack.removeFirst();
                            queue.add(o2);
                        }
                    }

                    stack.addFirst(o1);
                    break;
                }
                case Lparen: {
                    stack.addFirst(node);
                    break;
                }
                case Rparen: {
                    while (true) {
                        if (stack.isEmpty()) {
                            throw new ParserException("Expecting " + NodeType.Lparen);
                        }

                        Node first = stack.getFirst();

                        if (first.getType() == NodeType.Lparen) {
                            stack.removeFirst();

                            if (!stack.isEmpty() && stack.getFirst().getType() == NodeType.Function) {
                                Node fun = stack.getFirst();
                                stack.removeFirst();

                                queue.add(fun);
                            }

                            break;
                        } else {
                            stack.removeFirst();
                            queue.add(first);
                        }
                    }

                    break;
                }

            }
        }

        while(!stack.isEmpty()) {
            Node first = stack.getFirst();
            if(first.getType() == NodeType.Lparen ||
                    first.getType() == NodeType.Rparen) {
                throw new ParserException("Found no " + NodeType.Rparen);
            }

            stack.removeFirst();
            queue.add(first);
        }

        return new ArrayList<>(queue);
    }

    /**
     * Entscheidet, ob ein String ein Operator, eine Funktion, eine Klammer oder
     * ein Wert (Expression) ist, und gibt diesen als <code>Node</code> zurück.
     * @param token Der String
     * @return Einen identifizierten Knoten
     */
    private Node getNodeForToken(String token) {
        String tokenSanitized = token.trim().toLowerCase();
        for(Node b : buildins) {
            if(b.getIdentifier().toLowerCase().equals(tokenSanitized)) {
                return b;
            }
        }

        return new Node(token, NodeType.Expression);
    }
}
