package io.dapr.kubecon.examples.producer;

import io.dapr.springboot.DaprAutoConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(classes= {TestProducerApplication.class, DaprTestContainersConfig.class, DaprAutoConfiguration.class},
				webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ProducerAppTests {

	@Autowired
	private TestSubscriberRestController controller;

	@BeforeAll
	public static void setup(){
		org.testcontainers.Testcontainers.exposeHostPorts(8080);
	}

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost:" + 8080;
	}


	@Test
	void testOrdersEndpointAndMessaging() throws InterruptedException, IOException {
		given()
						.contentType(ContentType.JSON)
						.body(
										"""
                    {
                        "id": "abc-123",
                        "device": "gravitron-detector",
                        "payload": {
                        	"content": "captured data"
                        }
                    }
                    """
						)
						.when()
						.post("/device/events")
						.then()
						.statusCode(200);

		assertEquals(1, controller.getAllEvents().size());

		given()
						.contentType(ContentType.JSON)
						.when()
						.get("/device/events")
						.then()
						.statusCode(200).body("size()", is(1));

	}

}
