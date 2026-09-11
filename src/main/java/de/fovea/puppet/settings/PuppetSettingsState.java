package de.fovea.puppet.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.APP)
@State(name = "PuppetSettings", storages = @Storage("puppet.xml"))
public final class PuppetSettingsState implements PersistentStateComponent<PuppetSettingsState.StateData> {
    public static final class StateData {
        public String rubyExecutable = "auto";
        public String languageServerPath = "auto";
        public String pdkExecutable = "auto";
        public String puppetExecutable = "auto";
        public String puppetLintExecutable = "auto";
        public String modulePath = "";
        public String environmentPath = "";
        public String lspDebugLogFile = "";
    }

    private StateData state = new StateData();

    @Override
    public @Nullable StateData getState() { return state; }

    @Override
    public void loadState(@NotNull StateData state) { this.state = state; }

    public StateData data() { return state; }
}
