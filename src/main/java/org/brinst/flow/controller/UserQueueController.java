package org.brinst.flow.controller;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import org.brinst.flow.dto.AllowUserResponse;
import org.brinst.flow.dto.AllowedUserResponse;
import org.brinst.flow.dto.RankNumberResponse;
import org.brinst.flow.dto.RegisterUserResponse;
import org.brinst.flow.service.UserQueueService;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {
	private final UserQueueService userQueueService;

	@PostMapping("/register")
	public Mono<RegisterUserResponse> registerUser(
		@RequestParam(name = "queue", defaultValue = "default", required = false) String queue,
		@RequestParam(name = "user_id") Long userId) {
		return userQueueService.registerWaitQueue(queue, userId)
			.map(RegisterUserResponse::new);
	}

	@PostMapping("/allow")
	public Mono<AllowUserResponse> allowUser(
		@RequestParam(name = "queue", defaultValue = "default", required = false) String queue,
		@RequestParam(name = "count") Long count) {
		return userQueueService.allowUser(queue, count).map(allowed -> new AllowUserResponse(count, allowed));
	}

	@GetMapping("/allowed")
	public Mono<AllowedUserResponse> isAllowedUser(
		@RequestParam(name = "queue", defaultValue = "default", required = false) String queue,
		@RequestParam(name = "user_id") Long userId,
		@RequestParam(name = "token") String token) {
		return userQueueService.isAllowedByToken(queue, userId, token).map(AllowedUserResponse::new);
	}

	@GetMapping("/rank")
	public Mono<RankNumberResponse> getRankUser(
		@RequestParam(name = "queue", defaultValue = "default", required = false) String queue,
		@RequestParam(name = "user_id") Long userId) {
		return userQueueService.getRank(queue, userId).map(RankNumberResponse::new);
	}

	@GetMapping("/touch")
	Mono<String> touch(
		@RequestParam(name = "queue", defaultValue = "default", required = false) String queue,
		@RequestParam(name = "user_id") Long userId,
		ServerWebExchange exchange) {
		return Mono.defer(() -> userQueueService.generateToke(queue, userId))
			.map(token -> {
				exchange.getResponse().addCookie(
					ResponseCookie.from("user-queue-%s-token".formatted(queue), token)
						.maxAge(Duration.ofSeconds(300))
						.path("/")
						.build()
				);

				return token;
			});
	}

}


