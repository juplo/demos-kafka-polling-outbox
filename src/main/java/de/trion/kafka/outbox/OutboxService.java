package de.trion.kafka.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
public class OutboxService {

    private static final Logger LOG = LoggerFactory.getLogger(OutboxService.class);


    private final Map<String, String> state = new HashMap<>();
    private final Map<String, DeferredResult> requests = new HashMap<>();

    private long counter = 1;


    public OutboxService() {}


    public String bearbeiteVorgang(String vorgangId, String vbId, String data) {
        if (vorgangId == null)
            throw new IllegalArgumentException("vorgangId must not be null!");

        // Fehler beim Sichern simulieren
        Random r = new Random();
        int i = r.nextInt(10);
        if (i == 0)
            throw new RuntimeException("FEHLER!!!!!!");

        String result = vorgangId + "|vbId=" + vbId + "|" + counter++ + ", rand=" + i + ": " + data;

        if (state.containsKey(vorgangId))
            LOG.info("Bearbeite Vorgang {}: alt={}, neu={}", vorgangId, state.get(vorgangId), data);
        else
            LOG.info("Bearbeite Vorgang {}: neu={}", vorgangId, data);

        process(vorgangId, result);
        return result;
    }

    public synchronized void process(String vorgangId, DeferredResult result) {
        String data = state.get(vorgangId);
        if (data != null) {
            result.setResult(ResponseEntity.ok(data));
        }
        else {
            requests.put(vorgangId, result);
        }
    }

    private synchronized void process(String vorgangId, String result) {
        state.put(vorgangId, result);
        DeferredResult request = requests.get(vorgangId);
        if (request != null)
          request.setResult(ResponseEntity.ok(result));
    }
}
