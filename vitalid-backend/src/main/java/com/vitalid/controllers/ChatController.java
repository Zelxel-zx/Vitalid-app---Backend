package com.vitalid.controllers;

import com.vitalid.dtos.chat.ChatMessageRequest;
import com.vitalid.dtos.chat.ChatMessageResponse;
import com.vitalid.dtos.chat.UnreadResponse;
import com.vitalid.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chat Controller
 * Handles messaging between patients and doctors
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

	@Autowired
	private ChatService chatService;

	@GetMapping("/doctor/{doctorId}")
	public List<ChatMessageResponse> getMessagesByDoctor(
			@PathVariable Long doctorId,
			@RequestParam(value = "userId", required = false) Long userId) {
		return chatService.getMessagesByDoctorId(doctorId, userId);
	}

	@PostMapping("/send")
	public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageRequest request) {
		ChatMessageResponse response = chatService.sendMessage(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/unread")
	public List<UnreadResponse> getUnreadMessages(@RequestParam("receiverId") Long receiverId) {
		return chatService.getUnreadMessages(receiverId);
	}

	@GetMapping("/unread-count")
	public UnreadCountResponse getUnreadCount(@RequestParam("receiverId") Long receiverId) {
		return new UnreadCountResponse(chatService.getUnreadCount(receiverId));
	}

	@PutMapping("/read/{doctorId}")
	public ResponseEntity<MessageResponse> markMessagesAsRead(
			@PathVariable Long doctorId,
			@RequestParam("receiverId") Long receiverId) {
		chatService.markMessagesAsRead(doctorId, receiverId);
		return ResponseEntity.ok(new MessageResponse("Messages marked as read"));
	}

	public record MessageResponse(String message) {
	}

	public record UnreadCountResponse(long count) {
	}
}



