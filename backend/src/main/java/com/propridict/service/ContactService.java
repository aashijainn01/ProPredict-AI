package com.propridict.service;

import com.propridict.dto.ContactDTO;
import com.propridict.model.ContactRequest;
import com.propridict.repository.ContactRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRequestRepository contactRequestRepository;

    public ContactRequest submit(ContactDTO dto) {
        ContactRequest request = ContactRequest.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .purpose(dto.getPurpose())
                .preferredDate(dto.getPreferredDate())
                .preferredTime(dto.getPreferredTime())
                .message(dto.getMessage())
                .build();

        return contactRequestRepository.save(request);
    }
}
