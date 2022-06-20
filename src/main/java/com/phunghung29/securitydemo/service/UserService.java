package com.phunghung29.securitydemo.service;

import com.phunghung29.securitydemo.dto.LoginDto;
import com.phunghung29.securitydemo.dto.LoginRequestDto;
import com.phunghung29.securitydemo.dto.UserDto;
import com.phunghung29.securitydemo.entity.User;

import java.util.List;

public interface UserService {
    UserDto findById(Long id);
    List<UserDto> findAll();
    LoginDto login(LoginRequestDto loginRequestDto) throws RuntimeException;
}
