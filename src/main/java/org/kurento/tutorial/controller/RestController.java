package org.kurento.tutorial.controller;

import org.kurento.tutorial.api.CreaterLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@org.springframework.web.bind.annotation.RestController
public class RestController {

	@Autowired
	JavaMailSender mailSender;
	
	@GetMapping
	@RequestMapping("get-link")
	@ResponseBody
	public String getLink() {
		CreaterLink creator= new CreaterLink();
		return creator.getLink();
	}
	
	@GetMapping
	@RequestMapping("share-link")
	@ResponseBody
	public String ShareLink(@RequestParam String listEmail, @RequestParam String sender, @RequestParam String link) {
		if(listEmail.isEmpty()||sender.isEmpty()||link.isEmpty()) {
			return "failer";
		}
		String[] emails= listEmail.split(";");
		for(int i=0;i< emails.length;i++) {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(emails[i]);
			message.setSubject("Bạn đã nhận liên kết cuộc hội thoại từ"+ sender);
			message.setText(link);
			mailSender.send(message);
		}
		return "suscess";
	}
	
}
