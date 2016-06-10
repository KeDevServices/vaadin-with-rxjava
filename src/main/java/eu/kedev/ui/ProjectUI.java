package eu.kedev.ui;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import eu.kedev.ui.observer.NaiveAndUnsafeUiObserver;
import eu.kedev.ui.observer.NaiveButSafeUiObserver;
import eu.kedev.ui.observer.SafeUiObserver;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * @author Joachim Klein, jk(at)kedev.eu
 */
@Theme("valo")
@Push(PushMode.MANUAL) //you need to call ui.push() by yourself
public class ProjectUI extends UI {

	private static final long serialVersionUID = 1L;
	private static final int NUMBER_OF_EMITTED_EVENTS = 500;

    private final VerticalLayout view = new VerticalLayout();
    private final TabSheet tabSheet = new TabSheet();
    private final Map<ComponentContainer, Subscription> subscriptions = new HashMap<>();
    
    private final Button startNaiveUnsafeSame = new Button("Emit notifications naive and unsafe (Main Thread)");
    private final Button startNaiveUnsafeOther = new Button("Emit notifications naive and unsafe (RX Scheduler Thread)");
    private final Button startNaiveUnsafeMultiple = new Button ("Emit notifications naive and unsafe (multiple RX Scheduler Threads)"); 
    
    private final Button startNaiveButSafeOther = new Button("Emit notifications naive but safe (RX Scheduler Thread)");

    private final Button startSafeOther = new Button("Emit notifications safe (RX Scheduler Thread)");

    @Override
    protected void init(final VaadinRequest vaadinRequest) {
       view.setSpacing(true);
    	
        //Naive and unsafe
        view.addComponents(startNaiveUnsafeSame, startNaiveUnsafeOther, startNaiveUnsafeMultiple);
        startNaiveUnsafeSame.addClickListener(clickEvent -> {
        	final ComponentContainer tabContent = createResultTab("Naive, Unsafe, Main Thread");
        	final Subscription subscription = Events
        		.emittedViaObservableFast(NUMBER_OF_EMITTED_EVENTS)
        		.subscribe(new NaiveAndUnsafeUiObserver(tabContent));
        	this.subscriptions.put(tabContent, subscription);
        });
        
        startNaiveUnsafeOther.addClickListener(clickEvent -> {
        	final ComponentContainer tabContent = createResultTab("Naive, Unsafe, RX Scheduler Thread");
        	final Subscription subscription = Events
            		.emittedViaObservableFast(NUMBER_OF_EMITTED_EVENTS)
            		.subscribeOn(Schedulers.io())
            		.subscribe(new NaiveAndUnsafeUiObserver(tabContent));
        	this.subscriptions.put(tabContent, subscription);
        });
        	
        startNaiveUnsafeMultiple.addClickListener(clickEvent -> {
        	final ComponentContainer tabContent = createResultTab("Naive, Unsafe, Mult. RX Scheduler Threads");
        	final Subscription subscription = Observable.merge(Events.emittedViaObservableFast(NUMBER_OF_EMITTED_EVENTS)
					.window(10).map(o -> o.subscribeOn(Schedulers.computation())))
					.subscribeOn(Schedulers.io())
				.subscribe(new NaiveAndUnsafeUiObserver(tabContent));
        	this.subscriptions.put(tabContent, subscription);
        });
        
        //Naive but safe
        view.addComponent(startNaiveButSafeOther);
        startNaiveButSafeOther.addClickListener(clickEvent -> {
	    	final ComponentContainer tabContent = createResultTab("Naive, Safe, RX Scheduler Thread");
	    	final Subscription subscription = Events
	    		.emittedViaObservableFast(NUMBER_OF_EMITTED_EVENTS)
	    		.subscribeOn(Schedulers.io())
	    		.subscribe(new NaiveButSafeUiObserver(tabContent));
	    	this.subscriptions.put(tabContent, subscription);
	    });

        //Safe
        view.addComponent(startSafeOther);
        startSafeOther.addClickListener(clickEvent -> {
        	final ComponentContainer tabContent = createResultTab("Safe, RX Scheduler Thread");
        	final Subscription subscription = Events
    	    		.emittedViaObservableFast(NUMBER_OF_EMITTED_EVENTS)
    	    		.subscribeOn(Schedulers.io())
    	    		.subscribe(new SafeUiObserver(tabContent));
        	this.subscriptions.put(tabContent, subscription);
        });
        
        //Result area
        view.addComponent(tabSheet);
        tabSheet.setCloseHandler((tabsheet, tabContent) -> {
			subscriptions.get(tabContent).unsubscribe();
			subscriptions.remove(tabContent);
			tabsheet.removeComponent(tabContent);
		});
        
        setContent(view);
    }
    
    private VerticalLayout createResultTab(String caption) {
    	final VerticalLayout vl = new VerticalLayout();
    	vl.setMargin(true);
    	
    	final Tab t = tabSheet.addTab(vl, caption);
    	t.setClosable(true);
    	tabSheet.setSelectedTab(t);
    	
    	return vl;
    }
}
