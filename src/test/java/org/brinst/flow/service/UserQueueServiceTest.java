package org.brinst.flow.service;

import static org.junit.jupiter.api.Assertions.*;

import org.brinst.flow.EmbeddedRedis;
import org.brinst.flow.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;

@SpringBootTest
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
class UserQueueServiceTest {
	@Autowired
	private UserQueueService userQueueService;

	@Autowired
	private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

	@BeforeEach
	public void beforeEach() {
		ReactiveRedisConnection reactiveConnection = reactiveRedisTemplate.getConnectionFactory()
			.getReactiveConnection();
		reactiveConnection.serverCommands().flushAll().subscribe();
	}

	@Test
	void registerWaitQueue() {
		StepVerifier.create(userQueueService.registerWaitQueue("test", 100L))
			.expectNext(1L)
			.verifyComplete();

		StepVerifier.create(userQueueService.registerWaitQueue("test", 101L))
			.expectNext(2L)
			.verifyComplete();

		StepVerifier.create(userQueueService.registerWaitQueue("test", 102L))
			.expectNext(3L)
			.verifyComplete();
	}

	@Test
	void alreadyRegisteredWaitQueue() {
		StepVerifier.create(userQueueService.registerWaitQueue("test", 100L))
			.expectNext(1L)
			.verifyComplete();

		StepVerifier.create(userQueueService.registerWaitQueue("test", 100L))
			.expectError(ApplicationException.class)
			.verify();
	}

	@Test
	void emptyAllowUser() {
		StepVerifier.create(userQueueService.allowUser("test", 3L))
			.expectNext(0L)
			.verifyComplete();
	}

	@Test
	void allowUser() {
		StepVerifier.create(
				userQueueService.registerWaitQueue("test", 100L)
					.then(userQueueService.registerWaitQueue("test", 101L))
					.then(userQueueService.registerWaitQueue("test", 102L))
					.then(userQueueService.registerWaitQueue("test", 103L))
					.then(userQueueService.allowUser("test", 3L)))
			.expectNext(3L)
			.verifyComplete();

	}

	@Test
	void allowMoreUser() {
		StepVerifier.create(
				userQueueService.registerWaitQueue("test", 100L)
					.then(userQueueService.registerWaitQueue("test", 101L))
					.then(userQueueService.registerWaitQueue("test", 102L))
					.then(userQueueService.registerWaitQueue("test", 103L))
					.then(userQueueService.allowUser("test", 7L)))
			.expectNext(4L)
			.verifyComplete();
	}

	@Test
	void allowUserAfterRegisterWaitQueue() {
		StepVerifier.create(
				userQueueService.registerWaitQueue("test", 100L)
					.then(userQueueService.registerWaitQueue("test", 101L))
					.then(userQueueService.registerWaitQueue("test", 102L))
					.then(userQueueService.registerWaitQueue("test", 103L))
					.then(userQueueService.allowUser("test", 4L))
					.then(userQueueService.registerWaitQueue("test", 200L)))
			.expectNext(1L)
			.verifyComplete();
	}

	@Test
	void isNotAllowed() {
		StepVerifier.create(
				userQueueService.isAllowed("test", 100L))
			.expectNext(false)
			.verifyComplete();
	}

	@Test
	void isNotAllowed2() {
		StepVerifier.create(
				userQueueService.registerWaitQueue("test", 100L)
					.then(userQueueService.allowUser("test", 3L))
					.then(userQueueService.isAllowed("test", 101L)))
			.expectNext(false)
			.verifyComplete();
	}

	@Test
	void isAllowed() {
		StepVerifier.create(
				userQueueService.registerWaitQueue("test", 100L)
					.then(userQueueService.allowUser("test", 3L))
					.then(userQueueService.isAllowed("test", 100L)))
			.expectNext(true)
			.verifyComplete();
	}

	@Test
	void getRank() {
		StepVerifier.create(
				userQueueService.registerWaitQueue("test", 100L)
					.then(userQueueService.getRank("test", 100L)))
			.expectNext(1L)
			.verifyComplete();

		StepVerifier.create(
				userQueueService.registerWaitQueue("test", 101L)
					.then(userQueueService.getRank("test", 101L)))
			.expectNext(2L)
			.verifyComplete();
	}

	@Test
	void emptyRank() {
		StepVerifier.create(
				userQueueService.getRank("test", 100L))
			.expectNext(-1L)
			.verifyComplete();
	}

	@Test
	void isAllowedByToken() {
		StepVerifier.create(userQueueService.isAllowedByToken("test",100L,"d4437f2cc1f6b281e606460a4804049a2f48d7f2c106b0a7e557b7968efc1159"))
			.expectNext(true)
			.verifyComplete();
	}

	@Test
	void generateToke() {
		StepVerifier.create(userQueueService.generateToke("test",100L))
			.expectNext("d4437f2cc1f6b281e606460a4804049a2f48d7f2c106b0a7e557b7968efc1159")
			.verifyComplete();
	}
}
