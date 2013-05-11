package dk.hlyh.ciplugins.prereqbuildstep;

import hudson.model.Descriptor;
import hudson.tasks.Builder;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

/**
 * Prerequisite build step 
 *
 * @author Henrik Lynggaard
 */
public final class PrereqDescriptor extends Descriptor<Builder> {

    public PrereqDescriptor() {
        super(PrereqBuilder.class);
        load();
    }

    @Override
    public boolean configure(final StaplerRequest req, final JSONObject formData) {
        save();
        return true;
    }

    @Override
    public String getHelpFile() {
        return "/plugin/prereq-buildstep/help.html";
    }

    @Override
    public String getDisplayName() {
        return Messages.prereq_DisplayName();
    }

    @Override
    public PrereqBuilder newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
        return req.bindJSON(PrereqBuilder.class, formData);
    }

}
