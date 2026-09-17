package com.example.demo.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tutte le impostazioni di OpenRouter, lette da application.properties
 * sotto il prefisso "openrouter".
 */
@ConfigurationProperties(prefix = "openrouter")
public class OpenRouterProperties {

    /** Chiave segreta: arriva dalla variabile d'ambiente OPENROUTER_API_KEY. */
    private String apiKey;

    private String baseUrl = "https://openrouter.ai/api/v1";

    private String defaultModel;

    /** Titolo mostrato nella dashboard di OpenRouter. */
    private String appTitle = "ChatBot U6W1D3";

    /** Origine dichiarata a OpenRouter (header HTTP-Referer). */
    private String referer = "http://localhost:5173";

    /** Modelli selezionabili dal menu a tendina del frontend. */
    private List<ModelOption> models = List.of();

    public static class ModelOption {
        private String id;
        private String label;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public String getAppTitle() {
        return appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public List<ModelOption> getModels() {
        return models;
    }

    public void setModels(List<ModelOption> models) {
        this.models = models;
    }
}
