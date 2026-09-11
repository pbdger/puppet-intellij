package de.fovea.puppet.actions;

public final class PdkValidateAction extends AbstractPdkAction {
    @Override protected String[] arguments() { return new String[]{"validate"}; }
    @Override protected String title() { return "PDK Validate"; }
}
