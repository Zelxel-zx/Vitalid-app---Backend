package com.vitalid.chat.controller;

/*
 * Chat Controller
 * Handles messaging between patients and doctors
 * Routes: /api/chat, /api/messages
 * 
 * Endpoints:
 * GET  /api/chat/doctor/{doctorId} - Get messages for a specific doctor
 * POST /api/chat/send - Send a message from a user to a doctor
 * GET  /api/chat/unread - Get unread message count for all doctors
 * PUT  /api/chat/read/{doctorId} - Mark messages as read for a doctor
 * 
 * Records:
 * - SendMessageRequest(Long doctorId, Long senderId, String content)
 * - ChatMessageResponse(Long id, String sender, String content, String timestamp)
 * - UnreadResponse(Long doctorId, long unreadCount)
 * - MessageResponse(String message)
 */

