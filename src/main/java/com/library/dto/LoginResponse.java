package com.library.dto;

import com.library.enums.UserRole;

public record LoginResponse(String token, String username, UserRole role) {}
