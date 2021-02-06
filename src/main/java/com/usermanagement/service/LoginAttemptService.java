package com.usermanagement.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

	private static final int MAX_ATTEMPT_COUNT = 5;
	private static final int ATTEMPT_INCREMENT = 1;
	private final LoadingCache<String, Integer> cache;

	public LoginAttemptService() {
		super();
		this.cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
				.maximumSize(100)
				.build(new CacheLoader<String, Integer>() {
					@Override
					@ParametersAreNonnullByDefault
					public Integer load(String key) {
						return 0;
					}
				});
	}

	public void removeUserFromCache(String username) {
		cache.invalidate(username);
	}

	public void addUserToCache(String username) {
		int attempts = 0;
		try {
			attempts = cache.get(username) + ATTEMPT_INCREMENT;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		cache.put(username, attempts);
	}

	public boolean isMaxCountReached(String username) {
		try {
			return cache.get(username) >= MAX_ATTEMPT_COUNT;
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return false;
	}
}
