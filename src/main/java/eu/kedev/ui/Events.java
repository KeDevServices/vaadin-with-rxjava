package eu.kedev.ui;

import java.util.Random;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

/**
 * Utility class that holds methods for event stream generation.
 * 
 * @author Joachim Klein, jk(at)kedev.eu
 */
public class Events {
	
	private static final Random rand = new Random();
	
	/**
	 * Generates a given number of notifications. 
	 * 
	 * Goal: Simulates a long running evaluation (generating intermediate results)
	 * or reading of a stream of events (e.g., form an external resource).
	 */
	public static Observable<String> emittedViaObservableFast(int numberOfNotifications) {
		 return Observable.create(new OnSubscribe<String>() {		
			private Subscriber<? super String> subscriber;
			
			private Runnable generator = () -> {
				try {
					for (int i = 0; i < numberOfNotifications; i++) {
						//VERY IMPORTANT! Stops event creation if subscription ends
						if (subscriber.isUnsubscribed()) break;
						
						subscriber.onNext(String.format(
								"Notification %s: next event is emmitted immediately", i));
					}
					
					subscriber.onCompleted();
				} catch (Throwable t) {
					subscriber.onError(t);
				}
			};
			
			@Override
			public void call(Subscriber<? super String> subscriber) {
				this.subscriber = subscriber;
				new Thread(generator).start();
			}
		});
	}
	
	/**
	 * Generates a given number of notifications. 
	 * 
	 * Goal: Simulates a long running evaluation (generating intermediate results)
	 * or reading of a stream of events (e.g., form an external resource).
	 * 
	 * The generated notifications are provided via an {@linkplain rx.Observable}  
	 * and emitted every 10-50 milliseconds.
	 */
	public static Observable<String> emittedViaObservableSlow(int numberOfNotifications) {
		 return Observable.create(new OnSubscribe<String>() {		
			private Subscriber<? super String> subscriber;
			
			private Runnable generator = () -> {
				try {
					for (int i = 0; i < numberOfNotifications; i++) {
						//VERY IMPORTANT! Stops event creation if subscription ends
						if (subscriber.isUnsubscribed()) break;
						
						int sleepTime = rand.nextInt(40) + 10;
						subscriber.onNext(String.format(
								"Notification %s: next event in: %s ms", i, sleepTime));
						Thread.sleep(sleepTime);
					}
					
					subscriber.onCompleted();
				} catch (Throwable t) {
					subscriber.onError(t);
				}
			};
			
			@Override
			public void call(Subscriber<? super String> subscriber) {
				this.subscriber = subscriber;
				new Thread(generator).start();
			}
		});
	}
}
