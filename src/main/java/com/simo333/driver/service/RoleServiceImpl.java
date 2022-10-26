package com.simo333.driver.service;

import com.simo333.driver.model.Role;
import com.simo333.driver.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;


    @Override
    public List<Role> findAll() {
        return repository.findAll();
    }

    @Override
    public Role findOne(String name) {
        return repository.findByName(name).orElseThrow(() -> {
            log.error("Role with name '{}' not found", name);
            return new ResourceNotFoundException(String.format("Role with name '%s' not found", name));
        });
    }

    @Override
    public Role findOne(Long roleId) {
        return repository.findById(roleId).orElseThrow(() -> {
            log.error("Role with id '{}' not found", roleId);
            return new ResourceNotFoundException(String.format("Role with id '%s' not found", roleId));
        });
    }

    @Transactional
    @Override
    public Role save(Role role) {
        return repository.save(role);
    }

    @Transactional
    @Override
    public Role update(Role role) {
        findOne(role.getId());
        return repository.save(role);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
