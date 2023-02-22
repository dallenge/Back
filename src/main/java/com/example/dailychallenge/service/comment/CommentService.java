package com.example.dailychallenge.service.comment;

import com.example.dailychallenge.dto.CommentDto;
import com.example.dailychallenge.entity.challenge.Challenge;
import com.example.dailychallenge.entity.comment.Comment;
import com.example.dailychallenge.entity.comment.Comment.CommentBuilder;
import com.example.dailychallenge.entity.comment.CommentImg;
import com.example.dailychallenge.entity.users.User;
import com.example.dailychallenge.exception.AuthorizationException;
import com.example.dailychallenge.exception.comment.CommentCreateNotValid;
import com.example.dailychallenge.exception.comment.CommentNotFound;
import com.example.dailychallenge.repository.CommentRepository;
import com.example.dailychallenge.vo.ResponseChallengeComment;
import com.example.dailychallenge.vo.ResponseUserComment;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentImgService commentImgService;

    public Comment saveComment(CommentDto commentDto, List<MultipartFile> commentImgFiles, User user,
                               Challenge challenge) {

        isCommentContentOrImagesNotNull(commentDto, commentImgFiles);

        CommentBuilder commentBuilder = Comment.builder();
        if (commentDto != null) {
            commentBuilder
                    .content(commentDto.getContent());
        }
        Comment comment = commentBuilder
                .build();

        comment.saveCommentChallenge(challenge);
        comment.saveCommentUser(user);

        commentRepository.save(comment);

        if (commentImgFiles != null) {
            for (MultipartFile commentImgFile : commentImgFiles) {
                CommentImg commentImg = new CommentImg();
                commentImg.saveComment(comment);
                commentImgService.saveCommentImg(commentImg, commentImgFile);
            }
        }
        return comment;
    }

    private void isCommentContentOrImagesNotNull(CommentDto commentDto, List<MultipartFile> commentImgFiles) {
        if (commentDto == null && commentImgFiles == null) {
            throw new CommentCreateNotValid();
        }
    }

    public Comment updateComment(Long challengeId, Long commentId, CommentDto commentDto, User user) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFound::new);
        validateOwner(user, comment);
        validateChallenge(challengeId, comment);

        comment.updateComment(commentDto.getContent());

        return comment;
    }

    public void deleteComment(Long challengeId, Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFound::new);
        validateOwner(user, comment);
        validateChallenge(challengeId, comment);

        commentRepository.delete(comment);
    }

    public Integer likeUpdate(Long commentId, Integer isLike) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(CommentNotFound::new);
        if (isLike==1) {
            comment.updateLike(true);
        } else if(isLike == 0){
            comment.updateLike(false);
        }
        return comment.getLikes();
    }

    public Page<ResponseChallengeComment> searchCommentsByChallengeId(Challenge challenge, Pageable pageable) {

        Long challengeId = challenge.getId();
        return commentRepository.searchCommentsByChallengeId(challengeId, pageable);
    }

    public Page<ResponseUserComment> searchCommentsByUserId(Long userId, Pageable pageable) {

        return commentRepository.searchCommentsByUserId(userId, pageable);
    }

    public void validateOwner(User user, Comment comment) {
        if (!comment.isOwner(user.getId())) {
            throw new AuthorizationException();
        }
    }

    public void validateChallenge(Long challengeId, Comment comment) {
        if (!comment.isValidChallenge(challengeId)) {
            throw new AuthorizationException();
        }
    }
}
