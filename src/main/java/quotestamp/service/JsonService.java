package quotestamp.service;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class JsonService {
    private static final Logger LOG = Logger.getLogger(JsonService.class);

    /**
     * Returns a list of playlist ids parsed from a stream
     *
     * @param reader input stream reader containing playlist ids in json format
     * @return list of playlist ids
     */
    public List<String> getPlaylistIdsFromStream(InputStreamReader reader) {
        List<String> ids = new ArrayList<>();
        try {
            for (Object json : (JSONArray)((JSONObject)new JSONParser().parse(reader)).get("playlists")) {
                ids.add((String)((JSONObject)json).get("id"));
            }
        } catch (Exception e) {
            LOG.error("Failed to parse the playlist json file", e);
            throw new RuntimeException(e);
        }
        return ids;
    }
}
