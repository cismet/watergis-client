/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.watergis.profile;

import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class QpCheckResult {

    //~ Instance fields --------------------------------------------------------

    private List<ErrorResult> errors = new ArrayList<>();
    private int correct;
    private int incorrect;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the errors
     */
    public List<ErrorResult> getErrors() {
        return errors;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  error  the errors to set
     */
    public void addErrors(final ErrorResult error) {
        this.errors.add(error);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the correct
     */
    public int getCorrect() {
        return correct;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  correct  the correct to set
     */
    public void setCorrect(final int correct) {
        this.correct = correct;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the incorrect
     */
    public int getIncorrect() {
        return incorrect;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  incorrect  the incorrect to set
     */
    public void setIncorrect(final int incorrect) {
        this.incorrect = incorrect;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class ErrorResult {

        //~ Instance fields ----------------------------------------------------

        private int line;
        private String errorText;

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the line
         */
        public int getLine() {
            return line;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  line  the line to set
         */
        public void setLine(final int line) {
            this.line = line;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the errorText
         */
        public String getErrorText() {
            return errorText;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  errorText  the errorText to set
         */
        public void setErrorText(final String errorText) {
            this.errorText = errorText;
        }
    }
}
