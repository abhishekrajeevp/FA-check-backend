package com.bp.middleware.google;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/google")
public class GoogleController {

 @GetMapping("/map")
 public String googleView() {
	 return "GoogleMap";
 }
 
 
 
 
 
 
 
}
