package com.googledrive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.googledrive.listener.GoogleDriveListener;

@RestController
public class GoogleDriveController {

	@Autowired
	GoogleDriveListener googleDriveListener;

	@GetMapping(value = "/g-drive")
	public ResponseEntity<?> receiveGMessage(@RequestParam String credUrl, @RequestParam String admin)
			throws Exception {

		googleDriveListener.gmailDriveListener(credUrl, admin);
		return ResponseEntity.status(HttpStatus.OK).body(new ModelMap().addAttribute("msg", "Success"));
	}

}
