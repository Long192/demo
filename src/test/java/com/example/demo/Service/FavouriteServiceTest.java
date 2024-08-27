package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.Model.Favourite;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;
import com.example.demo.Repository.FavouriteRepository;

@SpringBootTest
public class FavouriteServiceTest {
    User user = User.builder().id(1L).email("email").password("password").fullname("fullname").build();
    Post post = Post.builder().id(1L).content("content").build();
    Favourite favourite = Favourite.builder().user(user).post(post).build();

    @InjectMocks
    private FavouriteService favouriteService;
    @Mock
    private FavouriteRepository favouriteRepository;

    @Test
    public void findFavouriteByUserIdSuccess() {
        when(favouriteRepository.findByUserId(anyLong())).thenReturn(List.of(favourite));

        List<Favourite> favourites = favouriteService.findFavouriteByUserId(1L);

        assertNotNull(favourites);
    }

    @Test
    public void saveSuccess() {
        favouriteService.save(favourite);

        ArgumentCaptor<Favourite> favouriteCaptor = ArgumentCaptor.forClass(Favourite.class);

        verify(favouriteRepository, times(1)).save(favouriteCaptor.capture());

        Favourite favouriteCaptured = favouriteCaptor.getValue();

        assertNotNull(favouriteCaptured);
        assertEquals(favouriteCaptured, favourite);
    }

    @Test
    public void deleteSuccess() {
        favouriteService.delete(favourite);

        ArgumentCaptor<Favourite> favouriteCaptor = ArgumentCaptor.forClass(Favourite.class);

        verify(favouriteRepository, times(1)).delete(favouriteCaptor.capture());

        Favourite favouriteCaptured = favouriteCaptor.getValue();

        assertNotNull(favouriteCaptured);
        assertEquals(favouriteCaptured, favourite);
    }

    @Test
    public void findByIdSuccess() {
        when(favouriteRepository.findById(anyLong())).thenReturn(Optional.of(favourite));

        Optional<Favourite> favourite = favouriteService.findById(1L);
        assertNotNull(favourite);
    }

    @Test
    public void findByUserIdAndPostIdSuccess() {
        when(favouriteRepository.findByUserIdAndPostId(anyLong(), anyLong())).thenReturn(Optional.of(favourite));

        Optional<Favourite> favourite = favouriteService.findByUserIdAndPostId(1L, 1L);
        assertNotNull(favourite);
    }
}