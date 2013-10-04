/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.gui.components;

import java.awt.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * A JTextField which checks if its content matches a regular expression and signalizes it, if this is not the case.
 *
 * @author   Gilles Baatz
 * @version  $Revision$, $Date$
 */
public class ValidationJTextField extends JTextField {

    //~ Instance fields --------------------------------------------------------

    private Pattern pattern = Pattern.compile(".*");

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ValidationJTextField object.
     */
    public ValidationJTextField() {
        init();
    }

    /**
     * Creates a new ValidationJTextField object.
     *
     * @param  text  DOCUMENT ME!
     */
    public ValidationJTextField(final String text) {
        super(text);
        init();
    }

    /**
     * Creates a new ValidationJTextField object.
     *
     * @param  columns  DOCUMENT ME!
     */
    public ValidationJTextField(final int columns) {
        super(columns);
        init();
    }

    /**
     * Creates a new ValidationJTextField object.
     *
     * @param  text     DOCUMENT ME!
     * @param  columns  DOCUMENT ME!
     */
    public ValidationJTextField(final String text, final int columns) {
        super(text, columns);
        init();
    }

    /**
     * Creates a new ValidationJTextField object.
     *
     * @param  doc      DOCUMENT ME!
     * @param  text     DOCUMENT ME!
     * @param  columns  DOCUMENT ME!
     */
    public ValidationJTextField(final Document doc, final String text, final int columns) {
        super(doc, text, columns);
        init();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     */
    private void init() {
        this.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void changedUpdate(final DocumentEvent e) {
                    validateText();
                }

                @Override
                public void removeUpdate(final DocumentEvent e) {
                    validateText();
                }

                @Override
                public void insertUpdate(final DocumentEvent e) {
                    validateText();
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pattern  DOCUMENT ME!
     */
    public void setPattern(final Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  regEx  DOCUMENT ME!
     */
    public void setPattern(final String regEx) {
        this.pattern = Pattern.compile(regEx);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isContentValid() {
        final String selectedItem = this.getText();
        final Matcher m = pattern.matcher(selectedItem);
        return m.matches();
    }

    /**
     * DOCUMENT ME!
     */
    private void validateText() {
        if (isContentValid()) {
            this.setBackground(Color.white);
        } else {
            this.setBackground(Color.red);
        }
    }
}
