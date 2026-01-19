package com.company.erm.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Settings shortcuts configuration properties.
 */
@ConfigurationProperties(prefix = "settings")
@Validated
public class SettingsShortcutsProperties {

    @NotEmpty(message = "settings.shortcuts must not be empty")
    private List<Shortcut> shortcuts = new ArrayList<>();

    public List<Shortcut> getShortcuts() {
        return shortcuts;
    }

    public void setShortcuts(List<Shortcut> shortcuts) {
        this.shortcuts = shortcuts;
    }

    public static class Shortcut {
        @NotBlank(message = "shortcut key is required")
        private String key;

        @NotBlank(message = "shortcut action is required")
        private String action;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
