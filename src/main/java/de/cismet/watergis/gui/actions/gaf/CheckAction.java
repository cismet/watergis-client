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
package de.cismet.watergis.gui.actions.gaf;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.event.ActionEvent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.cismet.tools.gui.StaticSwingTools;
import de.cismet.tools.gui.WaitingDialogThread;
import de.cismet.tools.gui.downloadmanager.DownloadManager;

import de.cismet.watergis.broker.AppBroker;
import de.cismet.watergis.gui.WatergisApp;

import de.cismet.watergis.gui.dialog.GafCheckDialog;

import de.cismet.watergis.utils.FakeFileDownload;
import de.cismet.watergis.utils.GafReader;
import java.util.ArrayList;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CheckAction extends AbstractAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CheckAction.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SelectionRectangleAction object.
     */
    public CheckAction() {
        final String tooltip = org.openide.util.NbBundle.getMessage(CheckAction.class,
                "CheckAction.toolTipText");
        putValue(SHORT_DESCRIPTION, tooltip);
        final String text = org.openide.util.NbBundle.getMessage(CheckAction.class,
                "CheckAction.text");
        putValue(NAME, text);
        final String mnemonic = org.openide.util.NbBundle.getMessage(CheckAction.class,
                "CheckAction.mnemonic");
        putValue(MNEMONIC_KEY, KeyStroke.getKeyStroke(mnemonic).getKeyCode());
        final ImageIcon icon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/watergis/res/icons16/icon-circledelete.png"));
        putValue(SMALL_ICON, icon);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent e) {
        GafCheckDialog.getInstance().setSize(195, 190);
        StaticSwingTools.showDialog(GafCheckDialog.getInstance());

        if (!GafCheckDialog.getInstance().isCancelled()) {
            final WaitingDialogThread<String[][]> wdt = new WaitingDialogThread<String[][]>(
                    StaticSwingTools.getParentFrame(AppBroker.getInstance().getWatergisApp()),
                    true,
                    NbBundle.getMessage(CheckAction.class, "CheckAction.actionPerformed.waitingDialog"),
                    null,
                    100,
                    true) {

                    @Override
                    protected String[][] doInBackground() throws Exception {
                        List<String[]> checkResult = new ArrayList<String[]>();
                        final File f = new File(GafCheckDialog.getInstance().getGafFile());

                        final GafReader reader = new GafReader(f);
                        final String rkFile = GafCheckDialog.getInstance().getRkFile();
                        final String bkFile = GafCheckDialog.getInstance().getBkFile();

                        if (rkFile != null) {
                            reader.addCustomCatalogue(new File(rkFile));
                        }

                        if (bkFile != null) {
                            reader.addCustomCatalogue(new File(bkFile));
                        }

                        checkResult.add(reader.checkFile());
                        
                        if (checkResult.get(0).length == 0) {
                            checkResult.add(reader.checkFileForHints());
                        }
                        
                        return checkResult.toArray(new String[checkResult.size()][]);
                    }

                    @Override
                    protected void done() {
                        try {
                            final String[][] checkResult = get();
                            String[] errors = checkResult[0];
                            String[] hints = null;
                            
                            if (checkResult.length > 1) {
                                hints = checkResult[1];
                            }
                            
                            if (errors.length > 0) {
                                handleErrors(errors);
                            } else if (hints != null && hints.length > 0) {
                                handleHints(hints);
                            } else {
                                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                                    NbBundle.getMessage(CheckAction.class, "CheckAction.actionPerformed().message"),
                                    NbBundle.getMessage(CheckAction.class, "CheckAction.actionPerformed().title"),
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (Exception e) {
                            LOG.error("Error while checking gaf profiles.", e);
                        }
                    }
                };

            wdt.start();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   errors  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void handleErrors(final String[] errors) throws IOException {
        handleCheckResults(errors, true);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param   hints  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void handleHints(final String[] hints) throws IOException {
        handleCheckResults(hints, false);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param   hints  DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    public static void handleCheckResults(final String[] hints, boolean errors) throws IOException {
        File errorPath = new File(GafCheckDialog.getInstance().getGafFile());
        String fileName = errorPath.getName();
        if (fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.indexOf("."));
        }
        
        if (fileName.equals("")) {
            fileName = WatergisApp.getDIRECTORYPATH_WATERGIS() + "/gaf";
        }

        errorPath = errorPath.getParentFile();
        final File errorFile = new File(errorPath, fileName + (errors ? "-fehler.txt": "-hinweise.txt"));
        boolean writeFile = false;

        if (errorFile.exists()) {
            final int ans = JOptionPane.showConfirmDialog(
                    AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(
                        CheckAction.class,
                        "CheckAction.actionPerformed().text",
                        errorFile.getAbsolutePath()),
                    NbBundle.getMessage(
                        DeleteAction.class,
                        "CheckAction.actionPerformed().title"),
                    JOptionPane.YES_NO_OPTION);

            if (ans == JOptionPane.YES_OPTION) {
                writeFile = true;
            }
        } else {
            writeFile = true;
        }

        if (writeFile) {
            BufferedWriter bw = null;

            try {
                bw = new BufferedWriter(new FileWriter(errorFile));
                for (final String error : hints) {
                    bw.write(error + System.lineSeparator());
                }
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }
            
            if (errors) {
                JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                    NbBundle.getMessage(
                        CheckAction.class,
                        "CheckAction.actionPerformed().error.message"),
                    NbBundle.getMessage(
                        CheckAction.class,
                        "CheckAction.actionPerformed().error.title"),
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
            JOptionPane.showMessageDialog(AppBroker.getInstance().getWatergisApp(),
                NbBundle.getMessage(
                    CheckAction.class,
                    "CheckAction.actionPerformed().hints.message"),
                NbBundle.getMessage(
                    CheckAction.class,
                    "CheckAction.actionPerformed().hints.title"),
                JOptionPane.INFORMATION_MESSAGE);
            }
            DownloadManager.instance().add(new FakeFileDownload(errorFile));
        }
    }

    @Override
    public boolean isEnabled() {
        return true || AppBroker.getInstance().isActionsAlwaysEnabled();
    }
}
