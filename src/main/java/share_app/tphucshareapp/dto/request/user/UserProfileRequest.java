package share_app.tphucshareapp.dto.request.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileRequest {

    @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
    private String firstName;

    @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
    private String lastName;

    @Size(max = 500, message = "Bio không được vượt quá 500 ký tự")
    private String bio;
}
