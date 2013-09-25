package org.vaadin.aceeditor;

/**
 * @author artamonov
 * @version $Id$
 */
public class TextUtils {
    public static boolean equals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }
}