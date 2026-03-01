package com.example.elderly.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderly.common.BusinessException;
import com.example.elderly.dto.EmergencyContactRequest;
import com.example.elderly.entity.EmergencyContact;
import com.example.elderly.mapper.EmergencyContactMapper;
import com.example.elderly.service.EmergencyContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyContactServiceImpl implements EmergencyContactService {

    private final EmergencyContactMapper contactMapper;

    @Override
    public EmergencyContact save(EmergencyContactRequest request) {
        EmergencyContact contact;
        if (request.getContactId() != null) {
            // update existing
            contact = contactMapper.selectById(request.getContactId());
            if (contact == null) {
                throw new BusinessException(404, "联系人不存在");
            }
            contact.setName(request.getName());
            contact.setPhone(request.getPhone());
            contact.setRelation(request.getRelation());
            contactMapper.updateById(contact);
        } else {
            // create new
            contact = new EmergencyContact();
            contact.setUserId(request.getUserId());
            contact.setName(request.getName());
            contact.setPhone(request.getPhone());
            contact.setRelation(request.getRelation());
            contactMapper.insert(contact);
        }
        return contact;
    }

    @Override
    public List<EmergencyContact> listByUser(Long userId) {
        QueryWrapper<EmergencyContact> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByAsc("contact_id");
        return contactMapper.selectList(wrapper);
    }

    @Override
    public void delete(Long contactId) {
        contactMapper.deleteById(contactId);
    }
}
