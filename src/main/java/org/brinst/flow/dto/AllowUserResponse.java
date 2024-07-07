package org.brinst.flow.dto;

public record AllowUserResponse(
	Long requestCount,
	Long allowedCount
) {
}
