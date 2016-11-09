package example;
/**
 * Example plug-in code for Phon which adds a "Hello World!" menu item
 * to all Phon windows.
 *
 */

import java.awt.Window;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ca.phon.plugin.*;

public class MyMenuFilter implements IPluginMenuFilter {

	@Override
	public void filterWindowMenu(Window owner, JMenuBar menuBar) {
		JMenu myMenu = new JMenu("Plug-in Example");
		menuBar.add(myMenu);
		
		JMenuItem item = new JMenuItem("Hello World!");
		myMenu.add(item);	
	}

}

public class MyMenuExtension implements IPluginExtensionPoint<IPluginMenuFilter>, IPluginExtensionFactory<IPluginMenuFilter> {

	@Override
	public Class<?> getExtensionType() { return IPluginMenuFilter; }

	@Override
	public IPluginMenuFilter createObject(Object... args) {
		return new MyMenuFilter();	
	}

	@Override
	public IPluginExtensionFactory<IPluginMenuFilter> getFactory() { return this; }

}

