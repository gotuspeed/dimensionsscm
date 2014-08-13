/* ===========================================================================
 *  Copyright (c) 2007, 2014 Serena Software. All rights reserved.
 *
 *  Use of the Sample Code provided by Serena is governed by the following
 *  terms and conditions. By using the Sample Code, you agree to be bound by
 *  the terms contained herein. If you do not agree to the terms herein, do
 *  not install, copy, or use the Sample Code.
 *
 *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
 *  shall have the nonexclusive, nontransferable right to use the Sample Code
 *  for the sole purpose of developing applications for use solely with the
 *  Serena software product(s) that you have licensed separately from Serena.
 *  Such applications shall be for your internal use only.  You further agree
 *  that you will not: (a) sell, market, or distribute any copies of the
 *  Sample Code or any derivatives or components thereof; (b) use the Sample
 *  Code or any derivatives thereof for any commercial purpose; or (c) assign
 *  or transfer rights to the Sample Code or any derivatives thereof.
 *
 *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
 *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
 *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
 *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
 *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
 *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
 *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
 *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
 *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
 *  REMAINS WITH YOU.
 *
 *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
 *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
 *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
 *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
 *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
 *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
 *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
 *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
 *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
 *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
 *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
 *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
 *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
 *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
 *
 *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
 *  harmless Serena from and against any and all liability, loss or claim
 *  arising from this agreement or from (i) your license of, use of or
 *  reliance upon the Sample Code or any related documentation or materials,
 *  or (ii) your development, use or reliance upon any application or
 *  derivative work created from the Sample Code.
 *
 *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
 *  license granted hereby shall terminate if and when your license to the
 *  applicable Serena software product terminates or if you breach any terms
 *  and conditions of this agreement.
 *
 *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
 *  Sample Code (collectively "Confidential Information") are the
 *  confidential information of Serena.  You agree to maintain the
 *  Confidential Information in strict confidence for Serena.  You agree not
 *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
 *  Confidential Information, in whole or in part, except as permitted in
 *  this Agreement.  You shall take all reasonable steps necessary to ensure
 *  that the Confidential Information is not made available or disclosed by
 *  you or by your employees to any other person, firm, or corporation.  You
 *  agree that all authorized persons having access to the Confidential
 *  Information shall observe and perform under this nondisclosure covenant.
 *  You agree to immediately notify Serena of any unauthorized access to or
 *  possession of the Confidential Information.
 *
 *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
 *  Inc. and its affiliates.  An entity shall be considered to be an
 *  affiliate of Serena if it is an entity that controls, is controlled by,
 *  or is under common control with Serena.
 *
 *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
 *  including any derivative works shall remain with Serena.  If a court of
 *  competent jurisdiction holds any provision of this agreement illegal or
 *  otherwise unenforceable, that provision shall be severed and the
 *  remainder of the agreement shall remain in full force and effect.
 * ===========================================================================
 */
package hudson.plugins.dimensionsscm;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor.FormException;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.VariableResolver;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * This experimental plugin extends Jenkins/Hudson support for Dimensions SCM
 * repositories.
 *
 * @author Tim Payne
 */
public class ArtifactUploader extends Notifier implements Serializable {
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    private String[] patternsRegEx = new String[0];
    private String[] patternsAnt = new String[0];

    private String[] patternsRegExExc = new String[0];
    private String[] patternsAntExc = new String[0];

    private boolean forceCheckIn;
    private boolean forceTip;
    private String owningPart;
    private boolean forceAsSlave;
    private String patternType = "regEx";

    public ArtifactUploader(String[] patternsRegEx, boolean fTip, boolean fMerge, String part) {
        this(patternsRegEx, fTip, fMerge, part, false, "regEx", null);
    }

    public ArtifactUploader(String[] patternsRegEx, boolean fTip, boolean fMerge, String part, boolean fAsSlave) {
        this(patternsRegEx, fTip, fMerge, part, fAsSlave, "regEx", null);
    }

    public ArtifactUploader(String[] patternsRegEx, boolean fTip, boolean fMerge, String part, boolean fAsSlave,
            String patternType, String[] pAnt) {
        this(patternsRegEx, fTip, fMerge, part, fAsSlave, patternType, pAnt, null, null);
    }

    @DataBoundConstructor
    public ArtifactUploader(String[] pregEx, boolean fTip, boolean fMerge, String part, boolean fAsSlave,
            String patternType, String[] pAnt, String[] pregExExc, String[] pAntExc) {
        // Check the folders specified have data specified.
        if (pregEx != null) {
            Logger.Debug("PatternsRegEx are populated");
            List<String> x = new ArrayList<String>(pregEx.length);
            for (String pattern : pregEx) {
                if (StringUtils.isNotEmpty(pattern)) {
                    x.add(pattern);
                }
            }
            this.patternsRegEx = x.toArray(new String[0]);
        } else {
            this.patternsRegEx[0] = ".*";
        }
        if (pAnt != null) {
            Logger.Debug("pAnt are populated");
            List<String> x = new ArrayList<String>(pAnt.length);
            for (String pattern : pAnt) {
                if (StringUtils.isNotEmpty(pattern)) {
                    x.add(pattern);
                }
            }
            this.patternsAnt = x.toArray(new String[0]);
        } else {
            this.patternsAnt[0] = "**/*";
        }

        if (pregExExc != null) {
            Logger.Debug("PatternsRegEx are populated");
            List<String> x = new ArrayList<String>(pregExExc.length);
            for (String pattern : pregExExc) {
                if (StringUtils.isNotEmpty(pattern)) {
                    x.add(pattern);
                }
            }
            this.patternsRegExExc = x.toArray(new String[0]);
        }
        if (pAntExc != null) {
            Logger.Debug("pAnt are populated");
            List<String> x = new ArrayList<String>(pAntExc.length);
            for (String pattern : pAntExc) {
                if (StringUtils.isNotEmpty(pattern)) {
                    x.add(pattern);
                }
            }
            this.patternsAntExc = x.toArray(new String[0]);
        }

        this.forceCheckIn = fTip;
        this.forceTip = fMerge;
        this.owningPart = part;
        this.forceAsSlave = fAsSlave;
        this.patternType = patternType;
    }

    /**
     * Gets the patterns to upload.
     */
    public String[] getPatternsRegEx() {
        return this.patternsRegEx;
    }

    /**
     * Gets the patterns to upload.
     */
    public String[] getPatternsAnt() {
        return this.patternsAnt;
    }

    /**
     * Gets the patterns to exclude from upload.
     */
    public String[] getPatternsRegExExc() {
        return this.patternsRegExExc;
    }

    /**
     * Gets the patterns to exclude from upload.
     */
    public String[] getPatternsAntExc() {
        return this.patternsAntExc;
    }

    /**
     * Gets the patterns to exclude from upload.
     */
    public String[] getPatternsExc() {
        if (getPatternType().equals("Ant")) {
            return this.patternsAntExc;
        } else {
            return this.patternsRegExExc;
        }
    }

    /**
     * Gets the patterns to upload.
     */
    public String[] getPatterns() {
        if (getPatternType().equals("Ant")) {
            return this.patternsAnt;
        } else {
            return this.patternsRegEx;
        }
    }

    /**
     * Gets the owning part name.
     */
    public String getOwningPart() {
        return this.owningPart;
    }

    /**
     * Gets the pattern type name.
     */
    public String getPatternType() {
        return this.patternType;
    }

    /**
     * Gets force checkin flag.
     * @return forceCheckIn
     */
    public boolean isForceCheckIn() {
        return this.forceCheckIn;
    }

    /**
     * Gets force merge flag.
     */
    public boolean isForceTip() {
        return this.forceTip;
    }

    /**
     * Gets force as slave flag.
     */
    public boolean isForceAsSlave() {
        return this.forceAsSlave;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws IOException, InterruptedException {
        long key = -1L;
        Logger.Debug("Invoking perform callout " + this.getClass().getName());
        FilePath workspace = build.getWorkspace();
        boolean bRet = false;
        boolean isStream = false;

        try {
            if (!(build.getProject().getScm() instanceof DimensionsSCM)) {
                listener.fatalError("[DIMENSIONS] This plugin only works with the Dimensions SCM engine.");
                build.setResult(Result.FAILURE);
                throw new IOException("[DIMENSIONS] This plugin only works with a Dimensions SCM engine");
            }

            if (build.getResult() == Result.SUCCESS) {
                DimensionsSCM scm = (DimensionsSCM) build.getProject().getScm();
                DimensionsAPI dmSCM = new DimensionsAPI();
                String nodeName = build.getBuiltOn().getNodeName();

                Logger.Debug("Calculating version of Dimensions...");

                int version = 2009;
                key = dmSCM.login(scm.getJobUserName(), scm.getJobPasswd(), scm.getJobDatabase(), scm.getJobServer(),
                        build);

                if (key > 0L) {
                    // Get the server version.
                    Logger.Debug("Login worked.");
                    version = dmSCM.getDmVersion();
                    if (version == 0) {
                        version = 2009;
                    }
                    if (version != 10) {
                        isStream = dmSCM.isStream(key, scm.getProjectName(build));
                    }
                    dmSCM.logout(key, build);
                }

                // Get the details of the master.
                InetAddress netAddr = InetAddress.getLocalHost();
                byte[] ipAddr = netAddr.getAddress();
                String hostname = netAddr.getHostName();

                String projectName = build.getProject().getName();
                int buildNo = build.getNumber();

                boolean master = true;
                if (isForceAsSlave()) {
                    master = false;
                    Logger.Debug("Forced processing as slave...");
                } else {
                    Logger.Debug("Checking if master or slave...");
                    if (nodeName != null && nodeName.length() > 0) {
                        master = false;
                    }
                }

                if (master) {
                    // Running on master...
                    listener.getLogger().println("[DIMENSIONS] Running checkin on master...");
                    listener.getLogger().flush();

                    // Using Java API because this allows the plugin to work on platforms where Dimensions has not
                    // been ported, e.g. MAC OS, which is what I use.
                    CheckInAPITask task = new CheckInAPITask(build, scm, buildNo, projectName, version, this,
                            workspace, listener);
                    bRet = workspace.act(task);
                } else {
                    // Running on slave... Have to use the command line as Java API will not work on remote hosts.
                    // Cannot serialise it...
                    // VariableResolver does not appear to be serialisable either, so...
                    VariableResolver<String> myResolver = build.getBuildVariableResolver();

                    String requests = myResolver.resolve("DM_TARGET_REQUEST");

                    listener.getLogger().println("[DIMENSIONS] Running checkin on slave...");
                    listener.getLogger().flush();

                    CheckInCmdTask task = new CheckInCmdTask(scm.getJobUserName(), scm.getJobPasswd(),
                            scm.getJobDatabase(), scm.getJobServer(), scm.getProjectName(build), requests,
                            isForceCheckIn(), isForceTip(), getPatterns(), getPatternType(), version, isStream,
                            buildNo, projectName, getOwningPart(), workspace, listener, getPatternsExc());
                    bRet = workspace.act(task);
                }
            } else {
                bRet = true;
            }
            if (!bRet) {
                build.setResult(Result.FAILURE);
            }
        } catch (Exception e) {
            listener.fatalError("Unable to load build artifacts into Dimensions - " + e.getMessage());
            build.setResult(Result.FAILURE);
            return false;
        }
        return bRet;
    }

    /**
     * The ArtifactUploader Descriptor class.
     */
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        /**
         * Loads the descriptor.
         */
        public DescriptorImpl() {
            super(ArtifactUploader.class);
            load();
            Logger.Debug("Loading " + this.getClass().getName());
        }

        public String getDisplayName() {
            return "Load any build artifacts into the Dimensions repository";
        }

        /**
         *  This builder can be used with all project types.
         */
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Save the descriptor configuration.
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindParameters(this, "ArtifactUploader");
            return super.configure(req, formData);
        }

        @Override
        public Notifier newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // Get variables and then construct a new object.
            String[] pregEx = req.getParameterValues("artifactuploader.patternsRegEx");
            String[] pAnt = req.getParameterValues("artifactuploader.patternsAnt");
            String[] pregExExc = req.getParameterValues("artifactuploader.patternsRegExExc");
            String[] pAntExc = req.getParameterValues("artifactuploader.patternsAntExc");
            String pType = req.getParameter("artifactuploader.patternType");
            Boolean fTip = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("artifactuploader.forceCheckIn")));
            Boolean fMerge = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("artifactuploader.forceTip")));

            String oPart = req.getParameter("artifactuploader.owningPart");
            Boolean fAsSlave = Boolean.valueOf("on".equalsIgnoreCase(req.getParameter("artifactuploader.forceAsSlave")));

            if (oPart != null) {
                oPart = Util.fixNull(req.getParameter("artifactuploader.owningPart").trim());
            }
            if (pType != null) {
                pType = Util.fixNull(req.getParameter("artifactuploader.patternType").trim());
            } else {
                pType = "regEx";
            }

            ArtifactUploader artifactor = new ArtifactUploader(pregEx, fTip, fMerge, oPart, fAsSlave, pType, pAnt,
                    pregExExc, pAntExc);
            return artifactor;
        }
    }
}