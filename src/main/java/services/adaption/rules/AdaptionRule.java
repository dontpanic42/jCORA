package services.adaption.rules;

/**
 * Created by daniel on 03.09.14.
 */
public interface AdaptionRule {

    /**
     * Gibt zurück, ob diese Regel eine lokale Adaptionsregel (<code>true</code>) oder eine
     * globale Adaptionsregel (<code>false</code>) ist.
     * @return <code>true</code>, wenn es sich um eine lokale Adaptionsregel handelt
     */
    public boolean isLocalAdaptionRule();

    /**
     * Gibt zurück, ob diese Regel eine lokale Adaptionsregel (<code>false</code>) oder eine
     * globale Adaptionsregel (<code>true</code>) ist.
     * @return <code>true</code>, wenn es sich um eine globale Adaptionsregel handelt
     */
    public boolean isGlobalAdaptionRule();

    /**
     * Gibt diese Regel als lokale Adaptionsregel zurück, oder wirt eine <code>NotImplementedException</code>,
     * wenn dies keine lokale Adaptionsregel ist.
     * @return Diese Regel als lokale Adaptionsregel
     */
    public LocalAdaptionRule asLocalAdaptionRule();

    /**
     * Gibt diese Regel als globale Adaptionsregel zurück, oder wirt eine <code>NotImplementedException</code>,
     * wenn dies keine global Adaptionsregel ist.
     * @return Diese Regel als globale Adaptionsregel
     */
    public GlobalAdaptionRule asGlobalAdaptionRule();

    /**
     * Gibt den Namen dieser Regel als <code>String</code> zurück.
     * @return Der Name der Regel
     */
    public String getRuleName();

    /**
     * Gibt einen kurze Beschreibung dieser Regel zurück
     * @return Ein String, der eine kurze Beschreibung der Regel zurückgibt
     */
    public String getRuleDescription();
}
