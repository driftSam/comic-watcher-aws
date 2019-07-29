package com.longbox.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.longbox.watcher.service.ComicWatcherService;

@SpringBootApplication
public class ComicWatcherApplication implements CommandLineRunner {

	@Autowired
	ComicWatcherService service;

	static final String directExchangeName = "comic-exchange";

	static final String queueName = "comic.found";

	private final RabbitTemplate rabbitTemplate;

	public ComicWatcherApplication(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Bean
	Queue queue() {
		return new Queue(queueName, false);
	}

	@Bean
	DirectExchange exchange() {
		return new DirectExchange(directExchangeName);
	}

	@Bean
	Binding binding(Queue queue, DirectExchange exchange) {
		return BindingBuilder.bind(queue).to(exchange).with("comic.found");
	}

	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		return container;
	}

	private static Logger LOG = LoggerFactory.getLogger(ComicWatcherApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ComicWatcherApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("watching");
		service.watch(rabbitTemplate);
	}

}
