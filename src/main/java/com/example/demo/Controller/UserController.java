package com.example.demo.Controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.Dto.Request.UpdateUserRequest;
import com.example.demo.Dto.Response.CustomResponse;
import com.example.demo.Dto.Response.UserDto;
import com.example.demo.Enum.OrderEnum;
import com.example.demo.Service.ExcelService;
import com.example.demo.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Tag(name = "user", description = "user")
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
    public ResponseEntity<CustomResponse<Page<UserDto>>> getAllUser(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") OrderEnum order
    ) throws Exception {
        Sort sort = Sort.by(Sort.Direction.fromString(order.toString()), sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);
        Page<UserDto> users = userService.findAll(pageable, search).map(source -> mapper.map(source, UserDto.class));
        return ResponseEntity.ok(CustomResponse.<Page<UserDto>>builder().data(users).build());
    }

    @Operation(summary = "report", description = "export excel file to show how many new post, like, comment, friend current logged in user have in previous week ")
    @GetMapping(value = "/report")
    public void report(HttpServletResponse response) throws Exception {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        excelService.exportUserReport(response);
    }

    @Operation(summary = "update user", description = "update user info")
    @PutMapping("")
    public ResponseEntity<CustomResponse<UserDto>> updateUserInfo(@RequestBody @Valid UpdateUserRequest req) throws Exception {
        return ResponseEntity.ok(CustomResponse.<UserDto>builder().data(userService.updateUser(req)).build());
    }
}
