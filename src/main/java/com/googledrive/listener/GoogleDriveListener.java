package com.googledrive.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Changes.Watch;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.Channel;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.googledrive.util.GenericRestCalls;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * https://search.google.com/search-console/about
 * 
 * https://developers.google.com/admin-sdk/reports/v1/guides/push
 * 
 * https://developers.google.com/drive/api/v2/reference/changes
 * 
 * @author gaian
 *
 */
@Slf4j
@Component
public class GoogleDriveListener {

	@Value("${gmail.api.scopes:https://www.googleapis.com/auth/drive.metadata.readonly,https://www.googleapis.com/auth/drive.appdata,https://www.googleapis.com/auth/drive.metadata,https://www.googleapis.com/auth/drive.photos.readonly,https://www.googleapis.com/auth/drive.appdata,https://www.googleapis.com/auth/drive,https://www.googleapis.com/auth/drive.file,https://www.googleapis.com/auth/drive.readonly,https://www.googleapis.com/auth/forms.body,https://www.googleapis.com/auth/forms.body.readonly,https://www.googleapis.com/auth/forms.responses.readonly,https://mail.google.com/,https://www.googleapis.com/auth/gmail.modify,https://www.googleapis.com/auth/gmail.readonly,https://www.googleapis.com/auth/cloud-platform,https://www.googleapis.com/auth/pubsub}")
	private String[] scopesForGmailApis;

	@Autowired
	private GenericRestCalls genericRestCalls;

	public void gmailDriveListener(String serviceAccFileUrl, String admin) throws IOException {

		String filePath = getServiceAccountDestailsAsStream(serviceAccFileUrl);

		GoogleCredential driveService = getGoogleCredential(filePath);

		// createFolder(driveService);

		Drive service = new Drive.Builder(new NetHttpTransport(), new JacksonFactory(), driveService).build();

		Channel channel = new Channel();
		channel.setAddress(
				"https://ingress-gateway.gaiansolutions.com/utility-service/social-engagement/g-drive/events");
		channel.setType("web_hook");
		channel.setId(UUID.randomUUID().toString());

		Watch action;
		try {
			action = service.changes().watch("161OSzYocfc4c0YoESLTOwhJ85-OhCaej", channel);
			System.out.println(action.execute().toPrettyString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String getServiceAccountDestailsAsStream(String serviceAccFileUrl) throws IOException {
		// TODO Auto-generated method stub
		log.info("----getServiceAccountDestailsAsStream : {}", serviceAccFileUrl);

		if (ObjectUtils.isEmpty(serviceAccFileUrl)) {
			throw new RuntimeException("File not found");
		}
		Path tempFile = Files.createTempFile("Google_Cred" + "_" + new Date().getTime(), ".json");
		String response = genericRestCalls.execute(serviceAccFileUrl, HttpMethod.GET, null, null, String.class);
		Files.write(tempFile, response.getBytes(StandardCharsets.UTF_8));
		log.info("-----File path  :{}", tempFile.getParent() + File.separator + tempFile.getFileName());

		return tempFile.getParent() + File.separator + tempFile.getFileName();
	}

	private GoogleCredential getGoogleCredential(String filePath) throws FileNotFoundException, IOException {
		HttpTransport httpTransport = new NetHttpTransport();
		GoogleCredential googleCredential = GoogleCredential
				.fromStream(new FileInputStream(filePath), httpTransport, new JacksonFactory())
				.createScoped(Arrays.asList(scopesForGmailApis));
		return googleCredential;
	}

	public CredentialsProvider getCredentialsProvider(String filePath) throws IOException {
		log.info("--------InputStream : {}", filePath);
		CredentialsProvider credentialsProvider = FixedCredentialsProvider
				.create(ServiceAccountCredentials.fromStream(new FileInputStream(filePath)));
		log.info("-----CredentialsProvider : {}", credentialsProvider);
		return credentialsProvider;
	}

	public String createFolder(GoogleCredential driveService) throws IOException {
		Drive service = new Drive.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance(), driveService)
				.setApplicationName("Drive samples").build();

		com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
		fileMetadata.setName("Test");
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		try {
			com.google.api.services.drive.model.File file = service.files().create(fileMetadata).setFields("id")
					.execute();
			System.out.println("Folder ID: " + file.getId());
			return file.getId();
		} catch (GoogleJsonResponseException e) {
			System.err.println("Unable to create folder: " + e.getDetails());
			throw e;
		}
	}

}
