package com.example.demo.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.Model.Comment;
import com.example.demo.Model.Post;
import com.example.demo.Model.User;

@SpringBootTest
public class ExcelServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private FavouriteService favouriteService;
    @Mock
    private FriendService friendService;
    @InjectMocks
    private ExcelService excelService;

    @BeforeEach
    void setUp() throws Exception {
        Post post = Post.builder().createdAt(new Timestamp(System.currentTimeMillis())).build();
        User user = User.builder().id(1L).posts(List.of(post)).build();
        Comment comment = Comment.builder().id(1L).user(user).post(post).createdAt(new Timestamp(System.currentTimeMillis())).build();

        user.setComments(List.of(comment));

        when(userService.findById(user.getId())).thenReturn(user);
        when(favouriteService.findFavouriteByUserId(user.getId())).thenReturn(new ArrayList<>());
        when(friendService.getFriendRaw()).thenReturn(new ArrayList<>());

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
    }

    @Test
    void exportUserReport_generatesExcelReport() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        excelService.exportUserReport(response);

        assertNotNull(response.getContentAsByteArray());

        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()));
        assertEquals(1, workbook.getNumberOfSheets());

        assertEquals("report", workbook.getSheetAt(0).getSheetName());
        assertEquals(2, workbook.getSheetAt(0).getPhysicalNumberOfRows()); // Header + content row

        // Verify content, e.g.:
        assertEquals("post written last week", workbook.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());

        workbook.close();
    }
}