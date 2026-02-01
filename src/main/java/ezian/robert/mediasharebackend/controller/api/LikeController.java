package ezian.robert.mediasharebackend.controller.api;

import ezian.robert.mediasharebackend.model.Like;
import ezian.robert.mediasharebackend.service.CommentServiceImpl;
import ezian.robert.mediasharebackend.service.LikeServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/like")
public class LikeController {
    private final LikeServiceImpl likeService;

    public LikeController(LikeServiceImpl likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public ResponseEntity<Like> addLike(@RequestBody Like like) {
       Like likesave = likeService.save(like);
       return ResponseEntity.status(HttpStatus.CREATED).body(likesave);
    }

}
