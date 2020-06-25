package com.mattermost.hackernewsbot.hackernewsbot;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@ComponentScan(basePackages = {"com.mattermost.hackernewsbot"})
@EnableScheduling
@Slf4j
@NoArgsConstructor
@RestController
@SpringBootApplication
public class HackernewsbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(HackernewsbotApplication.class, args);
	}

	@GetMapping("/")
	public String hello() {
		return "Hackernews Bot is running";
	}
}
