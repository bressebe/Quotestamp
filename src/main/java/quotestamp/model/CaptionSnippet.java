package quotestamp.model;

public class CaptionSnippet {
    private String id;
    private com.google.api.services.youtube.model.CaptionSnippet snippet;

    public CaptionSnippet(String id, com.google.api.services.youtube.model.CaptionSnippet snippet) {
        this.id = id;
        this.snippet = snippet;
    }

    public String getId() {
        return id;
    }

    public String getLanguage() {
        return snippet.getLanguage();
    }

    public String getTrackKind() {
        return snippet.getTrackKind();
    }
}
