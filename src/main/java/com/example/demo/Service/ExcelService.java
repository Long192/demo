package com.example.demo.Service;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.demo.Model.User;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ExcelService {
    @Autowired
    private UserService userService;
    @Autowired
    private FavouriteService favouriteService;
    @Autowired
    private FriendService friendService;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    public void exportUserReport(HttpServletResponse response) throws Exception {
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("report");
        createHeader();
        createContent();
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    private void createHeader() {
        Row row = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(14);
        style.setFont(font);
        createCell(row, 0, "post written last week", style);
        createCell(row, 1, "new friends last week", style);
        createCell(row, 2, "new like last week", style);
        createCell(row, 3, "new comment last week", style);
    }

    private void createContent() throws Exception {
        Row row = sheet.createRow(1);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(12);
        style.setFont(font);
        User me = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findById(me.getId());
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));
        int postCount = user.getPosts().stream().filter(post -> post.getCreatedAt().after(timestamp)).toList().size();
        int commentCount =
                user.getComments().stream().filter(comment -> comment.getCreatedAt().after(timestamp)).toList().size();
        int likeCount = favouriteService.findFavouriteByUserId(me.getId()).stream()
                .filter(like -> like.getCreatedAt().after(timestamp)).toList().size();
        int friendCount = friendService.getFriendRaw().stream().filter(friend -> friend.getCreatedAt().after(timestamp))
                .toList().size();
        createCell(row, 0, postCount, style);
        createCell(row, 1, friendCount, style);
        createCell(row, 2, likeCount, style);
        createCell(row, 3, commentCount, style);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }
}
