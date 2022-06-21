package com.phunghung29.securitydemo.service;

import com.phunghung29.securitydemo.dto.LoginDto;
import com.phunghung29.securitydemo.dto.LoginRequestDto;
import com.phunghung29.securitydemo.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserDto findById(Long id);
    List<UserDto> findAll();
    LoginDto login(LoginRequestDto loginRequestDto) throws RuntimeException;
    List<UserDto> findByAge(Integer age);
    Page<UserDto> findByAge(Integer age, Pageable pageable);
    List<UserDto> findByGender(String gender);
}
