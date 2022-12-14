package com.simo333.driver.service.impl;

import com.simo333.driver.exception.PasswordMismatchException;
import com.simo333.driver.exception.UserDuplicationException;
import com.simo333.driver.model.User;
import com.simo333.driver.payload.user.PasswordChangeRequest;
import com.simo333.driver.payload.user.UserUpdateRequest;
import com.simo333.driver.repository.UserRepository;
import com.simo333.driver.service.RefreshTokenService;
import com.simo333.driver.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RefreshTokenService tokenService;

    @Override
    public Page<User> findAll(Pageable page) {
        return userRepository.findAll(page);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> {
            log.error("User with username '{}' not found", username);
            return new ResourceNotFoundException(String.format("User with username '%s' not found", username));
        });
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("User with email '{}' not found", email);
            return new ResourceNotFoundException(String.format("User with email '%s' not found", email));
        });
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> {
            log.error("User with id '{}' not found", userId);
            return new ResourceNotFoundException(String.format("User with id '%s' not found", userId));
        });
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username);
    }

    @Transactional
    @Override
    public User save(User user) {
        checkUsernameAvailable(user.getUsername());
        checkEmailAvailable(user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        log.info("Saving a new user: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public User update(Long userId, UserUpdateRequest request) {
        User user = findById(userId);
        User updatedUser = applyUpdate(user, request);
        log.info("Updating user with id '{}'", user.getId());
        return userRepository.save(updatedUser);
    }

    @Transactional
    @Override
    public void changeUserPassword(PasswordChangeRequest patch) {
        User currentUser = getCurrentUser();
        if (!passwordEncoder.matches(patch.getOldPassword(), currentUser.getPassword())) {
            throw new PasswordMismatchException("Old password is incorrect");
        }
        currentUser.setPassword(passwordEncoder.encode(patch.getNewPassword()));
        log.info("Changing user's password. For user '{}'", currentUser.getUsername());
        userRepository.save(currentUser);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        tokenService.deleteByUser(findById(id));
        log.info("Deleting user with id '{}'", id);
        userRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return (User) authentication.getPrincipal();
        }
        throw new AccessDeniedException("Unauthorized");
    }

    private void checkUsernameAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new UserDuplicationException("User with this username already exists");
        }
    }

    private void checkEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserDuplicationException("User with this email already exists");
        }
    }

    private User applyUpdate(User user, UserUpdateRequest request) {
        if (request.getUsername() != null) {
            checkUsernameAvailable(request.getUsername());
            user.setUsername(request.getUsername());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getRoles() != null) {
            user.setRoles(request.getRoles());
        }
        if (request.getPoints() != null) {
            user.setPoints(request.getPoints());
        }
        return user;
    }
}
