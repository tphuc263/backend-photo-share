package share_app.tphucshareapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/photos")
@RequiredArgsConstructor
public class PhotoController {

}
