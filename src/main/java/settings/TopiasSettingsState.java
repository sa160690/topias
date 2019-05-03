package settings;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import settings.enums.DiscrType;

@State(name = "TopiasSettingsState",
        storages = {@Storage("topias_settings.xml")})
public class TopiasSettingsState implements PersistentStateComponent<TopiasSettingsState.SettingsState> {
    private SettingsState innerState = new SettingsState();

    @Override
    public void loadState(@NotNull SettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Nullable
    @Override
    public SettingsState getState() {
        return innerState;
    }

    @Override
    public void noStateLoaded() {
        System.out.println("No state was loaded");
    }

    public static TopiasSettingsState getInstance() {
        return ServiceManager.getService(TopiasSettingsState.class);
    }

    public static class SettingsState {
        public DiscrType discrType;

        public boolean showHistograms;

        SettingsState() {
            discrType = DiscrType.MONTH;
            showHistograms = true;
        }
    }
}