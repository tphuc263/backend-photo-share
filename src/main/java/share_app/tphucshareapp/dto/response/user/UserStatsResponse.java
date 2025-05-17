package share_app.tphucshareapp.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

    private long postCount;
    private long followerCount;
    private long followingCount;
}
