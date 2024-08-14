package com.example.demo.Controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Model.User;
import com.example.demo.Service.ExcelService;
import com.example.demo.Service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;

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
    public CustomResponse<List<UserDto>> getAllUser() {
        List<User> users = userService.findAll();
        return CustomResponse.<List<UserDto>> builder()
                .data(mapper.map(users, new TypeToken<List<UserDto>>() {}.getType())).build();
    }

    @Operation(summary = "report", description = "export excel file to show how many new post, like, comment, friend current logged in user have in previous week ")
    @GetMapping("/report")
    public void getMethodName(HttpServletResponse response) throws Exception {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        excelService.exportUserReport(response);
    }
}
