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
import java.util.Map;

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

	@PutMapping("/read/{doctorId}")
	public ResponseEntity<MessageResponse> markMessagesAsRead(
			@PathVariable Long doctorId,
			@RequestParam("receiverId") Long receiverId) {
		chatService.markMessagesAsRead(doctorId, receiverId);
		return ResponseEntity.ok(new MessageResponse("Messages marked as read"));
	}

	/**
	 * Returns the list of patients who have exchanged messages with a doctor.
	 * Used to render the doctor's inbox / conversation list.
	 */
	@GetMapping("/doctor/{doctorId}/conversations")
	public List<ChatService.ConversationSummary> getConversations(@PathVariable Long doctorId) {
		return chatService.getConversationsForDoctor(doctorId);
	}

	/** Simple total unread count for the nav badge. */
	@GetMapping("/unread-count")
	public Map<String, Long> getUnreadCount(@RequestParam("receiverId") Long receiverId) {
		long count = chatService.getTotalUnreadCount(receiverId);
		return Map.of("count", count);
	}

	public record MessageResponse(String message) {
	}
}



