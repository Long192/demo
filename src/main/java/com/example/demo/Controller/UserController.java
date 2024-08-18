package com.example.demo.Controller;

import com.example.demo.Dto.Request.UpdateUserRequest;
import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Dto.Response.MessageResponse;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Model.User;
import com.example.demo.Service.ExcelService;
import com.example.demo.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private ExcelService excelService;

    @Operation(summary = "get all user", description = "get all user")
    @GetMapping("")
    public ResponseEntity<CustomResponse<List<UserDto>>> getAllUser() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(CustomResponse.<List<UserDto>>builder()
            .data(mapper.map(users, new TypeToken<List<UserDto>>() {}.getType())).build());
    }

    @Operation(summary = "report", description = "export excel file to show how many new post, like, comment, friend current logged in user have in previous week ")
    @GetMapping(value = "/report")
    public void getMethodName(HttpServletResponse response) throws Exception {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        excelService.exportUserReport(response);
    }

    @Operation(summary = "update user", description = "update user info")
    @PutMapping(value = "", consumes = {"multipart/form-data"})
    public ResponseEntity<CustomResponse<MessageResponse>> updateUserInfo (
        @ModelAttribute UpdateUserRequest req
    ) throws Exception {
        userService.updateUser(req);
        return ResponseEntity.ok(CustomResponse.<MessageResponse>builder().data(new MessageResponse()).build());
    }
}
