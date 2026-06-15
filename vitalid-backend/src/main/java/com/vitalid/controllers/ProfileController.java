package com.vitalid.controllers;

import com.vitalid.dtos.profile.ProfileResponse;
import com.vitalid.dtos.profile.ProfileUpdateRequest;
import com.vitalid.dtos.profile.PasswordChangeRequest;
import com.vitalid.services.ProfileService;
import com.vitalid.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

/**
 * Profile Controller
 * Handles user profile information and settings
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {

	@Autowired
	private ProfileService profileService;

	@Autowired
	private UserRepository userRepository;

	@GetMapping
	public ProfileResponse getProfile(@RequestParam(value = "userId", required = false) Long userId) {
		Long resolvedUserId = resolveUserId(userId);
		return profileService.getProfile(resolvedUserId);
	}

	@PutMapping
	public ProfileResponse updateProfile(
			@RequestParam(value = "userId", required = false) Long userId,
			@RequestBody ProfileUpdateRequest request) {
		Long resolvedUserId = resolveUserId(userId);
		return profileService.updateProfile(resolvedUserId, request);
	}

	@PutMapping("/password")
	public ResponseEntity<MessageResponse> changePassword(
			@RequestParam(value = "userId", required = false) Long userId,
			@RequestBody PasswordChangeRequest request) {
		Long resolvedUserId = resolveUserId(userId);
		profileService.changePassword(resolvedUserId, request);
		return ResponseEntity.ok(new MessageResponse("Password updated"));
	}

	@PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadResponse uploadAvatar(
			@RequestParam(value = "userId", required = false) Long userId,
			@RequestParam("file") MultipartFile file) {
		Long resolvedUserId = resolveUserId(userId);
		return new UploadResponse(profileService.uploadAvatar(resolvedUserId, file));
	}

	@PostMapping(value = "/chat-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public UploadResponse uploadChatFile(@RequestParam("file") MultipartFile file) {
		return new UploadResponse(profileService.encodeUpload(file, false));
	}

	private Long resolveUserId(Long userId) {
		if (userId != null) {
			return userId;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getPrincipal() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
		}

		String email = authentication.getPrincipal().toString();
		return userRepository.findByEmail(email)
				.map(user -> user.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	public record MessageResponse(String message) {
	}

	public record UploadResponse(String url) {
	}
}



