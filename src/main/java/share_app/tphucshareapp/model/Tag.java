package share_app.tphucshareapp.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "tags")
public class Tag {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;
    private Instant createdAt;
}
