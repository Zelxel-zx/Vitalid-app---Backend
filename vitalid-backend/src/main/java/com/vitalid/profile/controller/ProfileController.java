package com.vitalid.profile.controller;

/*
 * Profile Controller
 * Handles user profile information and settings
 * 
 * Endpoints:
 * GET  /api/profile - Get user profile by userId
 * PUT  /api/profile - Update user profile (name, phone)
 * 
 * Records:
 * - ProfileUpdateRequest(String name, String phone)
 * - ProfileResponse(Long id, String name, String email, String phone, String type,
 *                   String bloodType, String address, String city, String state, String zipCode,
 *                   String medicalHistory, String allergies)
 * - MessageResponse(String message)
 */

