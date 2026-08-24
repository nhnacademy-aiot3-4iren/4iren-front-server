package com.nhnacademy.front.owner.service;

import com.nhnacademy.front.owner.client.OwnerClient;
import com.nhnacademy.front.owner.dto.AdminCreateRequest;
import com.nhnacademy.front.owner.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final OwnerClient ownerClient;

    public List<UserResponse> getAdmins() {
        return ownerClient.getUsers().getBody();
    }

    public void createAdmin(AdminCreateRequest request) {
        ownerClient.signUp(request);
    }

    public UserResponse getAdmin(Long userId) {
        return ownerClient.getUser(userId).getBody();
    }

    public void deleteAdmin(Long userId) {
        ownerClient.deleteUser(userId);
    }

    public void restoreAdmin(Long userId) {
        ownerClient.restoreUser(userId);
    }

    public void resetPassword(Long userId) {
        ownerClient.resetPassword(userId);
    }
}
