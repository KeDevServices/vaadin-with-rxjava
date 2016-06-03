package eu.kedev.ui.observer;

import com.google.gwt.thirdparty.guava.common.base.Preconditions;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

import rx.Observable;
import rx.Observer;

/**
 * <p>A Observer that receives notifications and push them to the UI.</p>
 * 
 * <p>Safe: It uses always a given valid UI reference.</p>
 *  
 * <p>Naive: Because it updates the UI in the same speed as events occur on 
 * the {@linkplain Observable} observed by this {@linkplain Observer}.</p>
 * 
 * <p>To overcome this problem see {@linkplain SafeUiObserver}.</p>
 * 
 * @author Joachim Klein, jk(at)kedev.eu
 */
public class NaiveButSafeUiObserver implements Observer<String> {
	
	private ComponentContainer targetContainer;
	private UI ui;
			
	public NaiveButSafeUiObserver(final ComponentContainer attachedTargetContainer) {
		this(attachedTargetContainer, attachedTargetContainer.getUI());
		//.getUI() is possible if the targetContainer is already attached to the UI.
	}
	
	public NaiveButSafeUiObserver(final ComponentContainer targetContainer, final UI ui) {
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
		ui.access(() -> {
			targetContainer.addComponent(new Label("Error emitting notifications: " + e));
			ui.push();
		});
		
	}

	@Override
	public void onNext(String notification) {
		//Of course, here I call UI.getCurrent() multiple times in an obvious boring way
		//to provoke the possible UIDetachedException. But, if you start subsequent processing
		//based on a result received from an Observable managed thread, it easily happens 
		//that you run in the similar problem with apparently better code.
		ui.access(() -> {
			targetContainer.addComponent(new Label(notification));
			ui.push();
		});
	}
}
