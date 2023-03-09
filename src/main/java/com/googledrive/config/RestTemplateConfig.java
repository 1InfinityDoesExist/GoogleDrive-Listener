package com.googledrive.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RestTemplateConfig {

	@Value("${rest.client.connection.timeout:60000}")
	private int restConnectionTimeout;

	@Value("${rest.read.timeout:60000}")
	private int restReadTimeout;

	@Value("${rest.connection.request.timeout:60000}")
	private int restConnectionRequestTimeout;

	/**
	 * Forms the request factory for HTTP requests
	 *
	 * @return HTTP request factory object
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 */
	public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory()
			throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {

		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		httpRequestFactory.setConnectTimeout(restConnectionTimeout);
		httpRequestFactory.setReadTimeout(restReadTimeout);
		httpRequestFactory.setConnectionRequestTimeout(restConnectionRequestTimeout);
		httpRequestFactory.setHttpClient(httpClient());
		return httpRequestFactory;

	}

	private CloseableHttpClient httpClient()
			throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
				.build();

		SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

		return HttpClients.custom().setSSLSocketFactory(csf).build();
	}

	@Bean("restTemplate")
	@Primary
	public RestTemplate restTemplate() throws KeyManagementException, KeyStoreException, NoSuchAlgorithmException {
		return new RestTemplate(clientHttpRequestFactory());
	}

}
