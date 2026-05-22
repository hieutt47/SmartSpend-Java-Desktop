package com.example.smartspend.service;

import java.util.prefs.Preferences;

/** Stores optional local-AI choices on this computer only. */
public class AiPreferencesService {
    private static final Preferences PREFS = Preferences.userRoot().node("smartspend/ai");
    private static final String ENABLE_LOCAL_AI = "enable_local_ai";
    private static final String PREFERRED_MODEL = "preferred_model";

    public boolean isLocalAiEnabled() {
        return PREFS.getBoolean(ENABLE_LOCAL_AI, true);
    }

    public void setLocalAiEnabled(boolean enabled) {
        PREFS.putBoolean(ENABLE_LOCAL_AI, enabled);
    }

    public String getPreferredModel() {
        return PREFS.get(PREFERRED_MODEL, "").trim();
    }

    public void setPreferredModel(String model) {
        if (model == null || model.isBlank()) {
            PREFS.remove(PREFERRED_MODEL);
        } else {
            PREFS.put(PREFERRED_MODEL, model.trim());
        }
    }
}
