/*
 * Copyright (c) 2011-2015 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.core.processor;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * @author Stephane Maldini
 */
@org.testng.annotations.Test
public class RingBufferWorkProcessorTests extends AbstractProcessorTests {

	@Override
	public Processor<Long, Long> createIdentityProcessor(int bufferSize) {
		return RingBufferWorkProcessor.<Long>create("tckRingBufferProcessor", bufferSize);
	}

	@Override
	public void required_mustRequestFromUpstreamForElementsThatHaveBeenRequestedLongAgo() throws Throwable {
		//IGNORE since subscribers see distinct data
	}

	@Override
	public void required_spec104_mustCallOnErrorOnAllItsSubscribersIfItEncountersANonRecoverableError() throws
			Throwable {
		for(int i = 0; i < 100; i++) {
			System.out.println("test "+i);
			super.required_spec104_mustCallOnErrorOnAllItsSubscribersIfItEncountersANonRecoverableError();
		}
	}

	public static void main(String... args){
		Publisher<Long> pub = s -> s.onSubscribe(new Subscription() {
			volatile boolean terminated = false;

			@Override
			public void request(long n) {
				if(!terminated) {
					for(long i = 0; i< n; i++) {
						s.onNext(i);
						try {
							Thread.sleep(1000);
						}catch(InterruptedException ie){
						}
					}
					terminated = true;
					s.onComplete();
				}
			}

			@Override
			public void cancel() {
				terminated = true;
			}
		});

		Processor<Long, Long> processor = RingBufferWorkProcessor.<Long>create();
		pub.subscribe(processor);
		processor.subscribe(new Subscriber<Long>() {
			@Override
			public void onSubscribe(Subscription s) {
				s.request(5);
			}

			@Override
			public void onNext(Long aLong) {
				System.out.println("next "+aLong);
			}

			@Override
			public void onError(Throwable t) {

			}

			@Override
			public void onComplete() {
				System.out.println("finish");
			}
		});
	}
}
