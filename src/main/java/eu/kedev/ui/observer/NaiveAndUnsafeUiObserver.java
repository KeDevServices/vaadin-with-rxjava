package eu.kedev.ui.observer;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.UIDetachedException;

import rx.Observable;
import rx.Observer;

/**
 * <p>A Observer that receives notifications and push them to the UI.</p>
 * 
 * <p>Unsafe: It calls {@linkplain UI#getCurrent()} within the implemented 
 * callback methods of {@linkplain Observer} to demonstrate that 
 * a {@linkplain UIDetachedException} can occur if the UI is accessed 
 * by threads that are not known/managed by Vaadin.</p>
 * 
 * <p>To overcome this problem see {@linkplain NaiveButSafeUiObserver}.</p>
 *  
 * <p>Naive: Because it updates the UI in the same speed as events occur on 
 * the {@linkplain Observable} observed by this {@linkplain Observer}.</p>
 * 
 * <p>To overcome this problem see {@linkplain SafeUiObserver}.</p>
 * 
 * @author Joachim Klein, jk(at)kedev.eu
 */
public class NaiveAndUnsafeUiObserver implements Observer<String> {
	
	private ComponentContainer targetContainer;	
	private UI ui;
	
	public NaiveAndUnsafeUiObserver(final ComponentContainer attachedTargetContainer) {
		this(attachedTargetContainer, attachedTargetContainer.getUI());
		//.getUI() is possible if the targetContainer is already attached to the UI.
	}
	
	public NaiveAndUnsafeUiObserver(final ComponentContainer targetContainer, final UI ui) {
		this.targetContainer = targetContainer;
		this.ui = Preconditions.checkNotNull(ui);
	}
	
	@Override
	public void onCompleted() {
		ui.access(() -> {
			targetContainer.addComponent(new Label("Emitting of items finished!"));
			ui.push();
		});
	}

	@Override
	public void onError(Throwable e) {
		//Safely implemented to see the error on the web-page
		ui.access(() -> {
			targetContainer.addComponent(new Label("Error emitting notifications: " + e));
			ui.push();
		});
		
	}

	@Override
	public void onNext(String notification) {
		//Here I call UI.getCurrent() multiple times in an obvious boring way
		//to provoke a UIDetachedException. If you start subsequent processing
		//based on a result received from an Observable managed thread, it easily 
		//happens that you run in the similar problem with apparently better code.
		UI.getCurrent().access(() -> {
			targetContainer.addComponent(new Label(notification));
			UI.getCurrent().push();
		});
	}
}
