package models.cbr;

/**
 * Created by daniel on 28.08.14.
 */
public class CoraRetrievalResult implements Comparable<CoraRetrievalResult> {

    private String caseId;
    private Float similarity;

    public CoraRetrievalResult(String caseId, float similarity) {
        this.caseId = caseId;
        this.similarity = similarity;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseName) {
        this.caseId = caseName;
    }

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    @Override
    public int compareTo(CoraRetrievalResult o) {
        return similarity.compareTo(o.getSimilarity());
    }
}
