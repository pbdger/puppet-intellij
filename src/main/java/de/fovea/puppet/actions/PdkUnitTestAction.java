package de.fovea.puppet.actions;

public final class PdkUnitTestAction extends AbstractPdkAction {
    @Override protected String[] arguments() { return new String[]{"test", "unit"}; }
    @Override protected String title() { return "PDK Unit Test"; }
}
