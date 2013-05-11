package dk.hlyh.ciplugins.prereqbuildstep;

import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.tasks.Builder;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Prerequisite build step.
 *
 * @author Henrik Lynggaard
 */
public final class PrereqBuilder extends Builder {

    @Extension
    public static final PrereqDescriptor DESCRIPTOR = new PrereqDescriptor();
    private static final boolean DEBUG = false;
    private final String projects;
    private final Boolean warningOnly;
    private final Boolean ignoreUnstableBuilds;

    public String getProjects() {
        return projects;
    }

    public Boolean getWarningOnly() {
        return warningOnly;
    }

    public Boolean getIgnoreUnstableBuilds() {
        return ignoreUnstableBuilds;
    }

    @DataBoundConstructor
    public PrereqBuilder(final String projects, final Boolean warningOnly, final Boolean ignoreUnstableBuilds) {
        this.projects = Util.fixEmptyAndTrim(projects);
        this.warningOnly = warningOnly;
        this.ignoreUnstableBuilds = ignoreUnstableBuilds;
    }

    @Override
    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }

    private boolean checkResult(final Result result) {
        if (ignoreUnstableBuilds) {
            return result.isWorseThan(Result.UNSTABLE);
        } else {
            return result.isWorseThan(Result.SUCCESS);
        }
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException {

        boolean checkPassed = true;

        PrintStream logger = listener.getLogger();
        long start = System.currentTimeMillis();
        List<Run<?, ?>> buildsChecked = new ArrayList<Run<?, ?>>();

        logger.println("Prerequisites check: projects='"+projects+"', warningOnly='"+warningOnly+"', ignoreUnstableBuilds='"+ignoreUnstableBuilds+"'");

        // Check jobs
        for (String projectName : projects.split(",")) {

            // find job
            AbstractProject<?, ?> upstreamJob = Hudson.getInstance().getItemByFullName(projectName, AbstractProject.class);
            if (upstreamJob == null) {

                logger.println("Prerequisites check: Job '" + projectName + "' not found");
                checkPassed = false;
                continue;
            }

            if (upstreamJob.isBuilding()) {
                logger.println("Prerequisites check: Job '" + projectName + "' is building");
                checkPassed = false;
                continue;
            }

            // find last run
            Run<?, ?> lastBuild = upstreamJob.getLastBuild();
            if (lastBuild == null) {
                logger.println("Prerequisites check: Job '" + projectName + "' has not been built");
                checkPassed = false;
                continue;
            }

            // check result
            Result result = lastBuild.getResult();
            logger.println("Prerequisites check: Job '" + lastBuild.getFullDisplayName() + "' has status " + result);

            if (checkResult(result)) {
                checkPassed = false;
                continue;
            }
        }

        boolean result = warningOnly || checkPassed;
        if (checkPassed == true) {
            logger.println("Prerequisites check: All checks passed ");
        } else if (!checkPassed && !warningOnly) {
            logger.println("Prerequisites check: Some checks failed ");
        } else if (!checkPassed && warningOnly) {
            logger.println("Prerequisites check: Some checks failed, but in warning only mode");
        }

        return result;

    }
}
