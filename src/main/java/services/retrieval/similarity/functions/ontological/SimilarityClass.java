package services.retrieval.similarity.functions.ontological;

import models.ontology.CoraClassModel;
import models.ontology.CoraPropertyModel;
import services.retrieval.similarity.functions.SimilarityFunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by daniel on 31.08.14.
 */
public class SimilarityClass extends SimilarityFunction<CoraClassModel> {

    @Override
    public Float calculateItemSim(CoraPropertyModel property, CoraClassModel a, CoraClassModel b) {

        System.out.println("Vergleiche Klassen: " + a + " und " + b + " (" + property + ")");

        float propSim = getPropertySimilarity(a, b);
        if(propSim == 0.f) {
            return 0.f;
        }

        float taxnSim = getTaxonomicSimilarity(a, b);


        System.out.println("Vergleiche Klassen: " + a + " und " + b + " (" + property + "): " + (taxnSim * propSim));
        return taxnSim * propSim;
    }

    /*******************************************************************************************************************
     * Klasseneigenschaften-Methoden
     ******************************************************************************************************************/

    /**
     * Berechnet die Ähnlichkeit der Klassenattribute:
     * - Wenn a und b beide KEINE Attribute haben: 1.0
     * - Wenn a oder b keine Eigenschaften haben: 0.0
     * - Wenn a und b keine gemeinsamen Eigenschaften haben: 0.0
     * - Sonst:
     * <code>
     *       (Anzahl gemeinsamer Eigenschaften) * 2       \n
     *   -------------------------------------------------\n
     *   (Anzahl Eigenschaften a + Anzahl Eigenschaften b)\n
     * </code>
     * @param a Die Eine Klasse
     * @param b Die Andere Klasse
     * @return Ähnlichkeit der Klassenattribute
     */
    private float getPropertySimilarity(CoraClassModel a, CoraClassModel b) {
        Set<CoraPropertyModel<?>> propsA = a.getProperties();
        Set<CoraPropertyModel<?>> propsB = b.getProperties();

        //Wenn BEIDE Klassen KEINE eigenschaften haben, gebe 1.f zurück.
        if(propsA.size() == 0 && propsB.size() == 0) {
            return 1.f;
        }

        //Wenn EINE der beiden Klassen KEINE eigenschaften hat, gebe 0.f zurück.
        if( (propsA.size() == 0 && propsB.size() != 0) ||
            (propsB.size() == 0 && propsA.size() != 0)) {
            return 0.f;
        }

        Set<CoraPropertyModel<?>> commonProps = new HashSet<>(propsA);
        commonProps.retainAll(propsB);

        //Wenn keine gemeinsamen Eigenschaften vorhanden sind, gebe 0.f zurück.
        if(commonProps.size() == 0) {
            return 0.f;
        }

        float numA = propsA.size();
        float numB = propsB.size();
        float numC = commonProps.size();

        //Keine variable kann == 0 sein, daher keine division durch 0 möglich...
        return (numC * 2.f) / (numA + numB);
    }

    /*******************************************************************************************************************
     * Klassentiefe-Methoden
     ******************************************************************************************************************/

    /**
     * Gibt die taxonomische Ähnlichkeit der Klasse a und b zurück.
     * @param a Die Eine Klasse
     * @param b Die Andere Klasse
     * @return Die taxonomische Ähnlichkeit
     */
    public float getTaxonomicSimilarity(CoraClassModel a, CoraClassModel b) {
        float classDepth = (float) getLCSDepth(a, b);
        if(classDepth != 0.f) {
            return 1.f / classDepth;
        }

        return 0.f;
    }

    /**
     * Sucht diejenige Pfadkombination, für die
     * <code>max(prof(pfadA, lcs), prof(pfadB, lcs))</code>
     * minimal wird.
     * TODO: Eventuell parameterisieren, so das die minimale/maximale/durchschnittliche Kombination gefunden wird?
     * @param a
     * @param b
     * @return
     */
    public int getLCSDepth(CoraClassModel a, CoraClassModel b) {
        //Wenn a und b die gleiche Klasse sind, gib 1 zurück.
        if(a.equals(b)) {
            return 1;
        }

        List<Path> aRoot = getPathsToRoot(a);
        List<Path> bRoot = getPathsToRoot(b);

        int depth = Integer.MAX_VALUE;
        int tmp;
        for(Path pathA : aRoot) {
            for(Path pathB: bRoot) {
                tmp = pathA.getMaxLCSDepth(pathB);
                if(tmp < depth) {
                    depth = tmp;
                }
            }
        }

        return depth;
    }

    /**
     * Gibt alle Pfade von <code>classModel</code> zu <code>OWL:Thing</code> zurück.
     * @param classModel Die Ausgangsklasse
     * @return Alle Pfade
     */
    public List<Path> getPathsToRoot(CoraClassModel classModel) {
        List<Path> paths = new ArrayList<>();
        Path firstPath = new Path();
        firstPath.add(classModel);
        paths.add(firstPath);

        getPathsToRootRec(paths, firstPath, classModel);

//        System.out.println("Pfade gefunden: " + paths.size());
//        printPaths(classModel, paths);

        return paths;
    }

    /**
     * Nimt an, das <code>currentClass</code> bereits in <code>currentPath</code> vorhanden ist,
     * und das <code>currentPath</code> bereits in <code>list</code> vorhanden ist.
     * @param list Liste aller Pfade zu OWL:Thing
     * @param currentPath der aktuell verfolgte Pfad
     * @param currentClass die Klasse, deren Eltern bestimmt werden sollen
     * @return
     */
    private void getPathsToRootRec(List<Path> list, Path currentPath, CoraClassModel currentClass) {
        //Die aktuelle Klasse hat "Thing" als Elternklasse: Breche ab. (Thing ist als letztes Element
        //im Pfad implizit vorhanden)
        if(currentClass.getBaseObject().isHierarchyRoot()) {
//            System.out.println(currentClass + " ist Root element");
            return;
        }

        Set<CoraClassModel> parents = currentClass.getFlattenedParents();

        //Die aktuelle Klasse hat keine Eltern: Breche ab.
        if(parents.size() == 0) {
//            System.out.println(currentClass + " hat keine Eltern");
            return;
        }

        //Die aktuelle Klasse hat genau eine Elternklasse: Füge diese zum Pfad hinzu, und rufe
        //getPathsToRootRec mit der Elternklasse auf
        if(parents.size() == 1) {
            CoraClassModel parent = getFirstParent(parents);
//            System.out.println(currentClass + " hat 1 Elternelement: " + parent);
            currentPath.add(parent);
            getPathsToRootRec(list, currentPath, parent);
            return;
        }

        //Klasse hat mehr als eine Elternklasse: Teile den Pfad auf.
        boolean first = true;
        Path cloned = currentPath.clone();
//        System.out.println(currentClass + " hat " + parents.size() + " Elternelemente. ");
        for(CoraClassModel parent : parents) {
            if(first) {
                currentPath.add(parent);
                getPathsToRootRec(list, currentPath, parent);
                first = false;
            } else {
                Path newPath = cloned.clone();
                newPath.add(parent);
                list.add(newPath);
                getPathsToRootRec(list, newPath, parent);
            }
        }
    }

//    private void printPaths(CoraClassModel model, List<Path> paths) {
//        System.out.println("Pfade für " + model + ": ");
//        for(Path p : paths) {
//            System.out.println("\t" + p.toString());
//        }
//        System.out.println("Ende Pfade für " + model);
//        System.out.println("----------------------------------");
//    }

    /**
     * Gibt ein zufälliges Element aus einem Set zurück.
     * @param set Das Set
     * @return Ein zufälliges element.
     */
    private CoraClassModel getFirstParent(Set<CoraClassModel> set) {
        for(CoraClassModel c : set) {
            return c;
        }

        throw new IndexOutOfBoundsException();
    }

    /**
     * Repräsentiert einen Pfade von einer Klasse zu einer Anderen (OWL:Thing)
     */
    public class Path {
        private ArrayList<CoraClassModel> list = new ArrayList<>();

        /**
         * Erzeuge einen leeren Pfad
         */
        public Path() {

        }

        /**
         * Findet den LCS zweier Pfade und gibt die maximale Distanz zum LCS zurück.
         * Die maximale Distanz ist gleich
         * <code>dist = max(prof(lcs, a), prof(lcs, b))</code>
         * @param other
         * @return
         */
        public int getMaxLCSDepth(Path other) {
            Path bigger, smaller;
            if(getPathLength() > other.getPathLength()) {
                bigger = this;
                smaller = other;
            } else {
                bigger = other;
                smaller = this;
            }

            for(CoraClassModel c : bigger.asList()) {
                if(smaller.contains(c)) {
//                    System.out.println("LCS: " + c);
                    return Math.max(smaller.depth(c), bigger.depth(c)) + 1;
                }
            }

//            System.out.println("LCS: OWL:Thing");
            return bigger.getPathLength();
        }

        /**
         * Erzeuge einen Pfad von einer Liste
         * @param list
         */
        public Path(ArrayList<CoraClassModel> list) {
            this.list = list;
        }

        /**
         * Gibt eine flache Kopie dieses Pfades zurück
         * @return
         */
        public Path clone() {
            return new Path( (ArrayList<CoraClassModel>) list.clone());
        }

        /**
         * Fügt diesem Pfad ein Element hinzu
         * @param classModel
         */
        public void add(CoraClassModel classModel) {
            list.add(classModel);
        }

        /**
         * Gibt die Pfadlänge zurück. OWL:Thing wird als implizit vorhandenes Element
         * angenommen, daher ergibt sich die Pfadlänge als (Anzahl der Elemente + 1)
         * @return
         */
        public int getPathLength() {
            return list.size() + 1;
        }

        /**
         * Gibt den Pfad als Liste zurück (ohne OWL:Thing)
         * @return
         */
        public List<CoraClassModel> asList() {
            return list;
        }

        /**
         * Gibt den Pfad als String zurück - zum Debuggen.
         * @return
         */
        @Override
        public String toString() {
            String str = "Pfad: ";
            for(int i = 0; i < list.size(); i++) {
                str += list.get(i);
                if(i < (list.size() - 1)) {
                    str += " -> ";
                }
            }

            str += " (-> OWL:Thing)";

            return str;
        }

        /**
         * Gibt zurück, ob <code>c</code> in diesem Pfad vorhanden ist
         * @param c Die Klasse, deren Existenz überprüft werden soll
         * @return <code>true</code>, wenn c in diesem Pfad vorhanden ist
         */
        public boolean contains(CoraClassModel c) {
            return list.contains(c);
        }

        /**
         * Gibt die Tiefe des Elements <code>c</code> in diesem Pfad zurück,
         * oder <code>-1</code>, wenn das element nicht vorhanden ist.
         * @param c Die Klasse, deren Tiefe ermittelt werden soll
         * @return Die Tiefe oder <code>-1</code>, wenn nicht vohanden
         */
        public int depth(CoraClassModel c) {
            return list.indexOf(c);
        }
    }

}
