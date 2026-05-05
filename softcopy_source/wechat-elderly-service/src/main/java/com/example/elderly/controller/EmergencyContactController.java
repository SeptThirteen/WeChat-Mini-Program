package com.example.elderly.controller;

import com.example.elderly.common.ApiResponse;
import com.example.elderly.dto.EmergencyContactRequest;
import com.example.elderly.entity.EmergencyContact;
import com.example.elderly.service.EmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService contactService;

    @GetMapping("/api/contact/list")
    public ApiResponse<List<EmergencyContact>> list(@RequestParam("userId") Long userId) {
        return ApiResponse.success(contactService.listByUser(userId));
    }

    @PostMapping("/api/contact/save")
    public ApiResponse<EmergencyContact> save(@Valid @RequestBody EmergencyContactRequest request) {
        return ApiResponse.success(contactService.save(request));
    }

    @DeleteMapping("/api/contact/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        contactService.delete(id);
        return ApiResponse.success(null);
    }
}
