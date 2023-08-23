package br.com.felipe.FMToy.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class KafkaProducerConfig {
	@Value("${topic.log_infos}")
	private String topicLogsInfo;
	
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	public void sendMessage(String message){
		this.kafkaTemplate.send(topicLogsInfo, message);
		log.info("Enviando mensagem ao topico de Logs {}", message);
	}
}
