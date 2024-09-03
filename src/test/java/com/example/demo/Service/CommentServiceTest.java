package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.Dto.Request.AddCommentRequest;
import com.example.demo.Dto.Request.UpdateCommentRequest;
import com.example.demo.Exception.CustomException;
import com.example.demo.Model.Comment;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;
import com.example.demo.Repository.CommentRepository;

@SpringBootTest
public class CommentServiceTest {
    @InjectMocks
    private CommentService commentService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostService postService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper mapper;
    
    User user = User.builder().id(1L).email("email@email.com").build();
    AddCommentRequest addReq = AddCommentRequest.builder().content("content").postId(1L).build();
    UpdateCommentRequest updateReq = UpdateCommentRequest.builder().content("new content").build();
    Post post = Post.builder().id(1L).content("content").user(user).build();
    Comment updatedComment = Comment.builder().content("new content").post(post).user(user).build();
    Comment comment = Comment.builder().content("content").post(post).user(user).build();

    @BeforeEach
    public void setUp() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    @Test
    public void addCommentSuccess() throws Exception {
        when(postService.findById(addReq.getPostId())).thenReturn(post);
        when(userService.findById(user.getId())).thenReturn(user);

        commentService.addComment(addReq);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository, times(1)).save(commentCaptor.capture());

        Comment commentCaptured = commentCaptor.getValue();

        assertNotNull(commentCaptured);
        assertEquals(comment, commentCaptured);
    }

    @Test
    public void addCommentFailePostNotFound() throws Exception {
        when(postService.findById(post.getId())).thenThrow(new CustomException(404, "post not found"));

        CustomException exception = assertThrows(CustomException.class, () -> commentService.addComment(addReq));

        assertEquals(exception.getMessage(), "post not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void addCommentFailedUserNotFound() throws Exception {
        when(postService.findById(post.getId())).thenReturn(post);
        when(userService.findById(user.getId())).thenThrow(new CustomException(404, "user not found"));

        CustomException exception = assertThrows(CustomException.class, () -> commentService.addComment(addReq));

        assertEquals(exception.getMessage(), "user not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void deleteCommentSuccess() throws Exception {
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        
        commentService.removeComment(comment.getId());

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository, times(1)).delete(commentCaptor.capture());

        Comment commentCaptured = commentCaptor.getValue();

        assertNotNull(commentCaptured);
        assertEquals(comment, commentCaptured);
    }

    @Test
    public void deleteCommentSuccessDeleteByPostOwner() throws Exception {
        User user2 = User.builder().id(3L).email("test@test.com").fullname("fullname").build();
        Comment deniedComment = Comment.builder().id(2L).post(post).user(user2).build();
        when(commentRepository.findById(deniedComment.getId())).thenReturn(Optional.of(deniedComment));

        commentService.removeComment(deniedComment.getId());

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository, times(1)).delete(commentCaptor.capture());

        Comment commentCaptured = commentCaptor.getValue();

        assertNotNull(commentCaptured);
        assertEquals(deniedComment, commentCaptured);
    }

    @Test
    public void deleteCommentFailedCommentNotFound() throws Exception {
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> commentService.removeComment(comment.getId()));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "comment not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void deleteCommentFailedPermissionDenied() throws Exception {
        User user2 = User.builder().id(3L).email("test@test.com").fullname("fullname").build();
        Post post2 = Post.builder().id(3L).content("content").user(user2).build();
        Comment deniedComment = Comment.builder().id(2L).post(post2).user(user2).build();

        when(commentRepository.findById(deniedComment.getId())).thenReturn(Optional.of(deniedComment));

        CustomException exception =
                assertThrows(CustomException.class, () -> commentService.removeComment(deniedComment.getId()));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 403);
        assertEquals(exception.getMessage(), "cannot delete this comment");
    }

    @Test
    public void editCommentSuccess() throws Exception {
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        commentService.editComment(comment.getId(), updateReq);

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        verify(commentRepository, times(1)).save(commentCaptor.capture());

        Comment commentCaptured = commentCaptor.getValue();

        assertNotNull(commentCaptured);
        assertEquals(updatedComment, commentCaptured);
    }

    @Test
    public void editCommentFailedCommentNotFound() throws Exception {
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.empty());

        CustomException exception =
                assertThrows(CustomException.class, () -> commentService.editComment(comment.getId(), updateReq));

        assertNotNull(exception);
        assertEquals(exception.getMessage(), "comment not found");
        assertEquals(exception.getErrorCode(), 404);
    }

    @Test
    public void editCommentFailedPerMissionDenied() throws Exception {
        User user2 = User.builder().id(3L).email("email@test.com").fullname("fullname").build();
        Comment comment2 = Comment.builder().id(3L).content("content").user(user2).build();

        when(commentRepository.findById(comment2.getId())).thenReturn(Optional.of(comment2));

        CustomException exception =
                assertThrows(CustomException.class, () -> commentService.editComment(comment2.getId(), updateReq));

        assertNotNull(exception);
        assertEquals(exception.getErrorCode(), 403);
        assertEquals(exception.getMessage(), "cannot edit this comment");
    }
}
