package quotestamp.factory;

import com.google.api.services.youtube.model.Caption;
import com.google.api.services.youtube.model.CaptionListResponse;
import org.springframework.stereotype.Component;
import quotestamp.model.CaptionSnippet;

import java.util.ArrayList;
import java.util.List;

@Component
public class CaptionSnippetFactory {
    public List<CaptionSnippet> createList(CaptionListResponse response) {
        List<CaptionSnippet> list = new ArrayList<>();
        for (Caption item : response.getItems()) {
            list.add(new CaptionSnippet(item.getId(), item.getSnippet()));
        }
        return list;
    }
}
