package com.example.elderly.service;

import com.example.elderly.dto.EmergencyContactRequest;
import com.example.elderly.entity.EmergencyContact;

import java.util.List;

public interface EmergencyContactService {
    EmergencyContact save(EmergencyContactRequest request);

    List<EmergencyContact> listByUser(Long userId);

    void delete(Long contactId);
}
