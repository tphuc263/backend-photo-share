package share_app.tphucshareapp.dto.response.photo;

import lombok.Data;

import java.time.Instant;

@Data
public class PhotoResponse {
    private String id;
    private String userId;
    private String imageURL;
    private String caption;
    private Instant createdAt;
}
