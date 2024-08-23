package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.Dto.Request.AddCommentRequest;
import com.example.demo.Dto.Request.UpdateCommentRequest;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.Comment;
import com.example.demo.Model.User;
import com.example.demo.Repository.CommentRepository;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;

    public void addComment(AddCommentRequest request) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(postService.findById(request.getPostId()));
        comment.setUser(userService.findById(user.getId()));
        commentRepository.save(comment);
    }

    public void removeComment(Long id) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Comment comment = findById(id);
        if (comment.getUser().getId().equals(user.getId())
            || comment.getPost().getUser().getId().equals(user.getId())) {
            commentRepository.delete(comment);
            return;
        }
        throw new CustomException(403, "cannot delete this comment");
    }

    public void editComment(Long id, UpdateCommentRequest request) throws Exception {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Comment comment =
            commentRepository.findById(id).orElseThrow(() -> new CustomException(404,"comment not found"));
        if(!user.getId().equals(comment.getUser().getId())) {
            throw new CustomException(403, "cannot edit this comment");
        }
        comment.setContent(request.getContent());
        commentRepository.save(comment);
    }

    public Comment findById(Long id) throws Exception {
        return commentRepository.findById(id).orElseThrow(() -> new CustomException(404, "comment not found"));
    }
}
