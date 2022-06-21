package com.phunghung29.securitydemo.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.phunghung29.securitydemo.dto.LoginDto;
import com.phunghung29.securitydemo.dto.LoginRequestDto;
import com.phunghung29.securitydemo.dto.UserDto;
import com.phunghung29.securitydemo.entity.User;
import com.phunghung29.securitydemo.repository.RoleRepository;
import com.phunghung29.securitydemo.repository.UserRepository;
import com.phunghung29.securitydemo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static com.phunghung29.securitydemo.Util.Utils.loadProperties;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailService;

    @Override
    public UserDto findById(Long id) {
        UserDto userDto = new UserDto();
        User user = userRepository.findById(id).orElseThrow(RuntimeException::new);
        BeanUtils.copyProperties(user, userDto);
        userDto.setRoleName(user.getRole().getRoleName());
        return userDto;
    }

    @Override
    public List<UserDto> findAll() {
        List<User> userList = userRepository.findAll();
        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userList) {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user, userDto);
            userDto.setRoleName(user.getRole().getRoleName());
            userDtoList.add(userDto);
        }
        return userDtoList;
    }

    @Override
    public LoginDto login(LoginRequestDto loginRequestDto) throws RuntimeException {
        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();
        log.info("Email: {} is logging into system", email);
        try {
            if (authenticate(email, password)) {
                UserDetails userDetails = userDetailService.loadUserByUsername(email);
                User detectedUser = userRepository.findByEmail(email);
                Map<String, Object> payload = new HashMap<>();
                payload.put("id", detectedUser.getId());
                payload.put("email", detectedUser.getEmail());
                payload.put("role", detectedUser.getRole().getRoleName());
                String token = generateToken(payload, new org.springframework.security.core.userdetails.User(userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities()));
                return new LoginDto(token);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("LOGIN_FAILURE");
        }
    }

    @Override
    public List<UserDto> findByAge(Integer age) {
        List<User> userList = userRepository.findByLessOrEqualThanAge(age);
        if (userList == null || userList.isEmpty()) {
            throw new RuntimeException("NOT FOUND");
        }
        List<UserDto> userDtoList = new ArrayList<>();
        userList.forEach(item -> {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(item, userDto);
            userDto.setRoleName(item.getRole().getRoleName());
            userDtoList.add(userDto);
        });
        return userDtoList;
    }

    @Override
    public Page<UserDto> findByAge(Integer age, Pageable pageable) {
        Page<User> userPage = userRepository.findByLessOrEqualThanAge(age, pageable);
        if (userPage == null || userPage.getContent().isEmpty()) {
            throw new RuntimeException("NOT FOUND");
        }
        List<UserDto> userDtoList = new ArrayList<>();
        userPage.getContent().forEach(item -> {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(item, userDto);
            userDtoList.add(userDto);
        });
        return new PageImpl<>(userDtoList, pageable, userPage.getTotalElements());
    }

    @Override
    public List<UserDto> findByGender(String gender) {
        List<User> userList = userRepository.findByGender(gender);
        if (userList == null || userList.isEmpty()) {
            throw new RuntimeException("NOT FOUND");
        }
        List<UserDto> userDtoList = new ArrayList<>();
        userList.forEach(item -> {
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(item, userDto);
            userDto.setRoleName(item.getRole().getRoleName());
            userDtoList.add(userDto);
        });
        return userDtoList;
    }

    public boolean authenticate(String email, String password) throws Exception {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            SecurityContextHolder.getContext().setAuthentication(auth);
            return true;
        } catch (DisabledException e) {
            throw new DisabledException("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("INCORRECT_EMAIL_OR_PASSWORD", e);
        }
    }

    public static String generateToken(Map<String, Object> payload, org.springframework.security.core.userdetails.User user) {
        Properties prop = loadProperties("jwt.setting.properties");
        assert prop != null;
        String key = prop.getProperty("key");
        String accessExpired = prop.getProperty("access_expired");
        assert key != null;
        assert accessExpired != null;
        long expiredIn = Long.parseLong(accessExpired);
        Algorithm algorithm = Algorithm.HMAC256(key);

        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiredIn))
                .withClaim("user", payload)
                .sign(algorithm);
    }
}
