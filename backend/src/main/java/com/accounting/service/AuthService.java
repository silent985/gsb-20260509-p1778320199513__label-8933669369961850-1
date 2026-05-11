package com.accounting.service;

import com.accounting.dto.*;
import com.accounting.entity.User;
import com.accounting.exception.BusinessException;
import com.accounting.repository.UserRepository;
import com.accounting.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AccountService accountService;

    /**
     * 用户注册
     */
    @Transactional
    public UserDTO register(RegisterRequest request) {
        logger.info("User registration attempt: {}", request.getUsername());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }

        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .build();

        user = userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());

        // 初始化默认分类
        categoryService.initDefaultCategories(user);
        logger.info("Default categories initialized for user: {}", user.getUsername());

        // 初始化默认账户
        accountService.initDefaultAccount(user);
        logger.info("Default account initialized for user: {}", user.getUsername());

        return UserDTO.fromEntity(user);
    }

    /**
     * 用户登录
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        logger.info("User login attempt: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = tokenProvider.generateToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查并初始化用户的默认分类和账户（用于通过数据库创建的用户如admin）
        initializeUserDataIfNeeded(user);

        logger.info("User logged in successfully: {}", user.getUsername());

        return LoginResponse.of(token, tokenProvider.getExpirationMs(), UserDTO.fromEntity(user));
    }

    /**
     * 检查并初始化用户默认数据
     */
    private void initializeUserDataIfNeeded(User user) {
        try {
            // 检查用户是否有分类，如果没有则初始化
            if (categoryService.getUserCategories(user.getId()).isEmpty()) {
                categoryService.initDefaultCategories(user);
                logger.info("Default categories initialized for existing user: {}", user.getUsername());
            }
            
            // 检查用户是否有账户，如果没有则初始化
            if (accountService.getUserAccounts(user.getId()).isEmpty()) {
                accountService.initDefaultAccount(user);
                logger.info("Default account initialized for existing user: {}", user.getUsername());
            }
        } catch (Exception e) {
            logger.warn("Error initializing user data: {}", e.getMessage());
        }
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "未登录");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
