package eu.kedev.ui;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;
import javax.servlet.annotation.WebServlet;

/**
 * @author Joachim Klein, jk(at)kedev.eu
 */
@WebServlet(
    value = "/*",
    asyncSupported = true)
@VaadinServletConfiguration(
    productionMode = false,
    ui = ProjectUI.class)
public class ProjectServlet extends VaadinServlet {
	private static final long serialVersionUID = 1L;
}
