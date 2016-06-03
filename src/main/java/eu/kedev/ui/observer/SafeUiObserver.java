package eu.kedev.ui.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import rx.Observer;

/**
 * <p>A Observer that receives notifications and push them to the UI.</p>
 * 
 * <p>Safe: It uses always a given valid UI reference.</p>
 * 
 * <p>Not Naive: Because it updates (tries to update - just demo) the UI in a UI compatible speed.</p>
 * 
 * @author Joachim Klein, jk(at)kedev.eu
 */
public class SafeUiObserver implements Observer<String> {
	
	private static final Logger logger = LoggerFactory.getLogger(SafeUiObserver.class);
	
	private final StoppableUpdater updater = new StoppableUpdater();
	private boolean processingIsStarted = false;
	private ComponentContainer targetContainer;
	
	
	private UI ui;
			
	public SafeUiObserver(final ComponentContainer attachedTargetContainer) {
		this(attachedTargetContainer, attachedTargetContainer.getUI());
		//.getUI() is possible if the targetContainer is already attached to the UI.
	}
	
	public SafeUiObserver(final ComponentContainer targetContainer, final UI ui) {
		this.targetContainer = targetContainer;
		this.ui = Preconditions.checkNotNull(ui);
		
		targetContainer.addDetachListener(event -> updater.stop());
		if (!targetContainer.isAttached()) {
			//start if UI element is attached 
			targetContainer.addAttachListener(event -> this.startUpdater());
		} else {
			//start immediately
			this.startUpdater();
		}
	}
	
	private void startUpdater() {
		new Thread(updater.getRunnable()).start();
	}
	
	@Override
	public void onCompleted() {
		processingIsStarted = true;
		ui.access(() -> {
			targetContainer.addComponent(new Label("Emitting of items finished!"));
		});
	}

	@Override
	public void onError(Throwable e) {
		processingIsStarted = true;
		ui.access(() -> {
			targetContainer.addComponent(new Label("Error emitting notifications: " + e));
		});
	}

	@Override
	public void onNext(String notification) {
		processingIsStarted = true;
		ui.access(() -> {
			targetContainer.addComponent(new Label(notification));
		});
	}
	
	/**
	 * Updates the view every second.
	 */
	private final class StoppableUpdater {
		private boolean run = true;
		
		public Runnable getRunnable() {
			return new Runnable() {
				@Override
				public void run() {
					while(run) {
						if (processingIsStarted) {
							//Simple polling solution
							ui.access(() -> {
								try {
									ui.push();
								} catch (Exception e) {
									//Here, simple Error-Notification.
									//Note about exception handling:
									//A good idea is to throw an exception that is handled by 
									//the manager of the Subscription that was build applying this
									//SafeUiObserver. This allows to properly close RX processing
									//by calling Subscription.unsubscribe()
									logger.warn("StoppableUpdater: Update of UI failed", e);	
								}
							});
						}
						
						try { Thread.sleep(1000); } catch (InterruptedException e) { }
					}
				}
			};
		}
		
		public void stop() {
			run = false;
		}
	}
}
