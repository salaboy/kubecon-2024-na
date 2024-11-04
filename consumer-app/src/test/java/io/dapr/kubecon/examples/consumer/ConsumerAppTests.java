package io.dapr.kubecon.examples.consumer;

import io.dapr.spring.messaging.DaprMessagingTemplate;
import io.dapr.springboot.DaprAutoConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import java.io.IOException;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes= {TestConsumerApplication.class, DaprTestContainersConfig.class, ConsumerAppTestConfiguration.class, DaprAutoConfiguration.class},
				webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ConsumerAppTests {

	@Autowired
	private DaprMessagingTemplate<DeviceEvent> messagingTemplate;


	@BeforeAll
	public static void setup(){
		org.testcontainers.Testcontainers.exposeHostPorts(8081);
	}

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost:" + 8081;
	}


	@Test
	void testMessageConsumer() throws InterruptedException, IOException {

		messagingTemplate.send("topic", new DeviceEvent("abc-123", "gravitron-det", new Payload("test")));

		Awaitility.given()
						.pollInterval(Duration.ofSeconds(3))
						.atMost(Duration.ofSeconds(20))
						.ignoreExceptions()
						.untilAsserted(() -> given()
										.contentType(ContentType.JSON)
										.when()
										.get("/events")
										.then()
										.statusCode(200).body("size()", is(1)));



	}

}
