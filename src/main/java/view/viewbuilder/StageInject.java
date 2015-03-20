package view.viewbuilder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;

/**
 * Created by daniel on 23.10.14.
 */

//Retention: Mache annotation zur laufzeit verfügbar

/**
 * Annotation zur injektion der Stage bei neu erzeugten Views
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD})
public @interface StageInject { }
