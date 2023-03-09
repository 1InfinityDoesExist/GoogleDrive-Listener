package com.googledrive.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GenericRestCalls {

	@Qualifier("restTemplate")
	@Autowired
	private RestTemplate restTemplate;

	public <R> R execute(String url, HttpMethod method, HttpHeaders headers, Object payload, Class<R> responseType) {

		ResponseEntity<R> response = restTemplate.exchange(url, method, new HttpEntity<>(payload, headers),
				responseType);
		log.info("-----Response : {}", response);
		if (!response.getStatusCode().is2xxSuccessful() || !response.hasBody()) {
			throw new RuntimeException("-----Failed to call the rest api.----");
		}
		return responseType.cast(response.getBody());
	}
}
