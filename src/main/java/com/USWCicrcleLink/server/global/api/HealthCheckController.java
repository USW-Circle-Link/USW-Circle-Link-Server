package com.USWCicrcleLink.server.global.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/health-check")
@RequiredArgsConstructor
public class HealthCheckController {

	@GetMapping
	public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("OK");
	}
}
