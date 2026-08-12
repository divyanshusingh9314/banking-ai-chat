package com.truecodes.banking_ai_chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BankingAiChatApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingAiChatApplication.class, args);
	}

}
