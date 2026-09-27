package com.company.pda.application.auth.dto;

public record RegisterUserCommand(
    String username, String password, String fullName, String email) {}
