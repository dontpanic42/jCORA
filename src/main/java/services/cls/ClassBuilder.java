package services.cls;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import services.cls.parser.EvaluatorException;
import services.cls.parser.Parser;
import services.cls.parser.ParserException;
import services.cls.parser.Tokenizer;
import services.cls.parser.util.Node;
import services.cls.parser.util.ast.ASTNode;

import java.util.List;

/**
 * Created by daniel on 15.05.15.
 */
public class ClassBuilder {

    public static OntClass createClassFromString(OntModel model, String script)
            throws ParserException, EvaluatorException {
        Tokenizer tokenizer = new Tokenizer();
        String[] tokens = tokenizer.tokenize(script);

        Parser parser = new Parser();
        List<Node> nodes = parser.toRPN(tokens);
        ASTNode ast = parser.toAST(nodes);

        ClassBuilderEvaluator eval = new ClassBuilderEvaluator(model);
        OntResource res = eval.evaluate(ast);

        System.err.println("Evaluation result: " + res);

        return res.asClass();
    }
}
