package services.rules.jenarules;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;
import mainapp.MainApplication;
import models.cbr.CoraCaseModel;
import models.cbr.CoraCaseModelImpl;
import services.rules.RulesEngine;
import services.rules.jenarules.builtins.casebase.CBFindNodeWithProperty;
import services.rules.jenarules.builtins.casebase.math.CBAverageInteger;

/**
 * Created by daniel on 11.03.2015.
 */
@SuppressWarnings("ConstantConditions")
public class JenaRulesEngine extends RulesEngine {
    /**
     * Ist <code>onInfModel</code> gleich <code>true</code> werden die Regeln auf das Inferenzmodell
     * angewendet (ist extrem langsam!). Ist <code>onInfModel</code> gleich <code>true</code> wird
     * das basismodell verwendet.
     */
    private final boolean onInfModel = false;
    private final boolean logDiff = true;

    private static boolean builtinsInitialized = false;
    private static void initBuiltins() {
        if(builtinsInitialized) {
            return;
        }

        builtinsInitialized = true;

        BuiltinRegistry.theRegistry.register(new CBAverageInteger());
        BuiltinRegistry.theRegistry.register(new CBFindNodeWithProperty());
    }

    @Override
    public CoraCaseModel applyRuleset(CoraCaseModel baseCase, String ruleSetFileName) throws Exception {
        initBuiltins();

        CoraCaseModelImpl c = (CoraCaseModelImpl) baseCase;
        OntModel ontmodel = c.getModel();
        Model model = (onInfModel)? ontmodel : ontmodel.getBaseModel();

        /* Starte den RuleReasoner auf dem von Pellet erzeugten Inferenzmodell */
        Resource config = model.createResource();
        config.addProperty(ReasonerVocabulary.PROPruleMode, "hybrid");
        config.addProperty(ReasonerVocabulary.PROPruleSet, ruleSetFileName);

        Reasoner reasoner = GenericRuleReasonerFactory.theInstance().create(config);
        reasoner.setDerivationLogging(true);

        System.out.println("Starting rule reasoning...");
        InfModel m = ModelFactory.createInfModel(reasoner, model);
        /* Wende die Regeln an */
        m.prepare();
        /* Rule Reasoner ende */
        System.out.println("Finished rule reasoning...");

        Model outModel = m;
        if(outModel.getNsPrefixURI("") == null) {
            outModel.setNsPrefix("", "http://example.com/case#");
            System.out.println("Prefix ist null");
        }

        /* Erzeuge einen temporären Fall */
        CoraCaseModel tempModel = MainApplication.getInstance().getCaseBase().createTemporaryCase();
        Model base = ((CoraCaseModelImpl) tempModel).getModel();

        Model diff = m.difference(model);

        if(logDiff) {
            StmtIterator iter = diff.listStatements();
            while(iter.hasNext()) {
                System.out.println("Diff add: " + iter.next());
            }

            iter.close();
        }

        addToTemporary(base, model, diff);

        //m.close();
        diff.close();

        return tempModel;
    }

    /**
     * Kopiert <code>baseModel</code> und <code>derivation</code> in das Model <code>caseModel</code>.
     * @param caseModel Das Model, in das die anderen Modelle kopiert werden
     * @param baseModel Das erste zu kopierende Model
     * @param derivation Das zweite zu kopierende Model
     */
    private void addToTemporary(Model caseModel, Model baseModel, Model derivation) {

        caseModel.add(baseModel.listStatements());

        caseModel.add(derivation.listStatements());
    }

//    /**
//     * @deprecated Benutze InfModel#difference()
//     * TODO: Finde heraus, was #difference() macht, wenn das regel-builtin remove() aufgerufen wird...
//     * Gibt ein Model zurück, das alle Statements enthält, die in <code>newModel</code> aber nicht
//     * in <code>oldModel</code> enthalten sind. (Neue Statements)
//     * @param oldModel
//     * @param newModel
//     * @return
//     */
//    private Model getDerivationModel(Model oldModel, Model newModel) {
//        // An iterator over the statements of pModel that *aren't* in the base model.
//        ExtendedIterator<Statement> stmts = newModel.listStatements().filterDrop( new Filter<Statement>() {
//            @Override
//            public boolean accept(Statement o) {
//                return oldModel.contains( o );
//            }
//        });
//
//        // For convenient printing, we create a model for the deductions.
//        // (If stmts were a StmtIterator, we could add it directly.  It's not,
//        // so we end up creating a new StmtIteratorImpl, which is kludgey, but
//        // it's more efficient than converting the thing to a list.)
//        Model deductions = ModelFactory.createDefaultModel().add( new StmtIteratorImpl( stmts ));
//
//
//        return deductions;
//    }
}
