package com.personal.waiting_api;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Operators;
import reactor.core.publisher.Operators.MonoSubscriber;

//@SpringBootTest
class FlowApplicationTests {

	@Test
	void contextLoads() {
		Flux.just(1,2,3,4)
			.doOnNext(it-> System.out.println(it))
			.subscribe();
	}

}
