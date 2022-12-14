package com.simo333.driver.service;

import com.simo333.driver.model.User;
import com.simo333.driver.payload.user.PasswordChangeRequest;
import com.simo333.driver.payload.user.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    Page<User> findAll(Pageable page);

    User findById(Long userId);

    User findByUsername(String username);
    
    User findByEmail(String email);

    User save(User user);

    User update(Long userId, UserUpdateRequest request);

    void delete(Long id);

    void changeUserPassword(PasswordChangeRequest patch);
}
