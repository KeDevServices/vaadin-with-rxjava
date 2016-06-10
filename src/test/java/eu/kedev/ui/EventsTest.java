package eu.kedev.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;

import rx.Observable;

public class EventsTest {

	private static final Logger logger = LoggerFactory.getLogger(EventsTest.class);
	private static final int NUMBER_OF_NOTIFICATIONS = 5;

	@Test(timeout=(NUMBER_OF_NOTIFICATIONS*50 +100)) /* e.g., 5 notifications must 
		be emitted in at least 250ms (+100ms for the test itself) */
	public void testEmittedViaObservableSlow5Notifications() {
		assertEquals(NUMBER_OF_NOTIFICATIONS, 
				logAndCount(Events.emittedViaObservableSlow(NUMBER_OF_NOTIFICATIONS)));
	}
	
	@Test
	public void testEmittedViaObservableFast5Notifications() {
		assertEquals(NUMBER_OF_NOTIFICATIONS, 
				logAndCount(Events.emittedViaObservableFast(NUMBER_OF_NOTIFICATIONS)));
	}
	
	private static int logAndCount(final Observable<String> observable) {
		return observable
				.map(n -> {logger.debug(n); return n;})
				.count().toBlocking().first();
	}
}
