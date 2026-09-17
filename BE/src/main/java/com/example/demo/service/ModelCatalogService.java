package com.example.demo.service;

import com.example.demo.config.OpenRouterProperties;
import com.example.demo.dto.ModelOptionDTO;
import com.example.demo.dto.openrouter.ModelsResponse;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Tiene aggiornato l'elenco dei modelli mostrati nella tendina, scaricandolo
 * da OpenRouter invece di leggerlo da una lista scritta a mano.
 *
 * <p><strong>Solo modelli gratuiti.</strong> Un modello entra nell'elenco solo
 * se costano zero sia il prompt sia la risposta E l'id finisce per ":free".
 * Sono gli stessi filtri della pagina dei modelli di OpenRouter
 * (max_output_price=0, variant=free, output_modalities=text).
 *
 * <p><strong>Ordinamento.</strong> La pagina di OpenRouter ordina per latenza,
 * ma quel dato non e' esposto da nessuna API pubblica: il campo
 * latency_last_30m e' sempre vuoto sulle varianti gratuite. Usiamo allora la
 * dimensione del modello come indicatore: i parametri ricavati dal nome
 * (2.6b, 26b, 120b, 550b...) crescono insieme al tempo di risposta, quindi
 * "piu' piccolo prima" si avvicina a "piu' veloce prima". I modelli che non
 * dichiarano la dimensione nel nome finiscono in fondo.
 */
@Service
public class ModelCatalogService {

    private static final Logger log = LoggerFactory.getLogger(ModelCatalogService.class);

    /** Cattura "2.6b", "30b", "550b" dentro allo slug del modello. */
    private static final Pattern SIZE = Pattern.compile("(\\d+(?:\\.\\d+)?)b(?![a-z0-9])");

    private final RestClient client;
    private final OpenRouterProperties properties;

    /** volatile: scritta dal thread dello scheduler, letta da quelli HTTP. */
    private volatile List<ModelOptionDTO> catalog = List.of();

    public ModelCatalogService(RestClient openRouterClient, OpenRouterProperties properties) {
        this.client = openRouterClient;
        this.properties = properties;
    }

    /**
     * Parte subito all'avvio (initialDelay 0) e poi ogni 6 ore.
     * Gira su un thread dello scheduler, quindi non rallenta l'avvio dell'app.
     */
    @Scheduled(initialDelay = 0, fixedDelay = 6, timeUnit = TimeUnit.HOURS)
    public void refresh() {
        try {
            ModelsResponse response = client.get()
                    .uri("/models")
                    .retrieve()
                    .body(ModelsResponse.class);

            if (response == null || response.data() == null) {
                log.warn("Elenco modelli vuoto da OpenRouter: tengo quello precedente.");
                return;
            }

            List<ModelOptionDTO> updated = response.data().stream()
                    .filter(ModelCatalogService::isFreeTextModel)
                    .sorted(Comparator
                            .comparingDouble((ModelsResponse.Model m) -> parameterBillions(m.id()))
                            .thenComparing(ModelsResponse.Model::name,
                                    Comparator.nullsLast(String::compareToIgnoreCase)))
                    .map(m -> new ModelOptionDTO(m.id(), label(m)))
                    .toList();

            if (updated.isEmpty()) {
                log.warn("Nessun modello gratuito trovato: tengo l'elenco precedente.");
                return;
            }

            catalog = updated;
            log.info("Catalogo modelli aggiornato: {} modelli gratuiti.", updated.size());

        } catch (Exception e) {
            // Se OpenRouter non risponde l'app deve continuare a funzionare
            // con l'ultimo elenco valido, non piantarsi.
            log.warn("Impossibile aggiornare l'elenco modelli ({}): uso la cache.",
                    e.getMessage());
        }
    }

    /** L'elenco per il frontend. Se non e' mai riuscito a scaricare, la riserva. */
    public List<ModelOptionDTO> list() {
        List<ModelOptionDTO> current = catalog;
        if (!current.isEmpty()) {
            return current;
        }
        return properties.getModels().stream()
                .map(option -> new ModelOptionDTO(option.getId(), option.getLabel()))
                .toList();
    }

    // ---------- filtri e ordinamento ----------

    private static boolean isFreeTextModel(ModelsResponse.Model m) {
        if (m.id() == null || !m.id().endsWith(":free")) {
            return false;
        }
        var pricing = m.pricing();
        if (pricing == null || !isZero(pricing.prompt()) || !isZero(pricing.completion())) {
            return false;
        }
        var architecture = m.architecture();
        return architecture != null
                && architecture.output_modalities() != null
                && architecture.output_modalities().contains("text");
    }

    private static boolean isZero(String price) {
        if (price == null) {
            return false;
        }
        try {
            return Double.parseDouble(price) == 0d;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Miliardi di parametri ricavati dal nome, es. "…-30b-a3b-…" -> 30.
     * Chi non li dichiara va in fondo alla lista.
     */
    static double parameterBillions(String id) {
        String slug = id.substring(id.indexOf('/') + 1).replace(":free", "");
        Matcher matcher = SIZE.matcher(slug);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        }
        return Double.MAX_VALUE;
    }

    private static String label(ModelsResponse.Model m) {
        String name = m.name() != null ? m.name() : m.id();
        // Tutti i modelli qui sono gratuiti: ripeterlo in ogni voce e' rumore.
        return name.replace(" (free)", "").trim();
    }
}
