package me.lpk.gui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import me.lpk.gui.controls.TabContainer;

/**
 * TODO feature list for SkidGUI 2.0
 *
 * <ul>
 * <li>ZKM String removal / replacer
 * <li>Easy to use bytecode editor
 * <li>Visual class/field/method remapper (Like an interactive JD-GUI)
 * <li>Plug-in system for the following:
 * <ul>
 * <li>Custom obfuscation types
 * <li>Custom patch jobs
 * <li>Application CSS
 * </ul>
 * <li>Inserting / removing code presets
 * <ul>
 * <li>Have a database of bytecode sequences
 * <li>JD-GUI window that you can right click and insert in the presets
 * before/after lines
 * <li>Window remembers where presets are and can remove them instead of
 * removing each instruction individually
 * </ul>
 * <li>Database for known malicious instructions (Java malware scanner)
 * <li>String pattern search (Have a few templates such as websites and IP
 * addresses)
 * </ul>
 */
public class Main extends Application {
	private static final double VERSION = 2.0;
	private static File jarComparison, jarTarget;
	private static Set<TabContainer> tabs = new HashSet<TabContainer>();

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		BorderPane root = new BorderPane();
		TabPane tabPane = new TabPane();
		// Iterate and create TabContainers for each tab type
		for (EnumTab tabType : EnumTab.values()) {
			Tab tab = new Tab();
			tab.setText(tabType.getName());
			tab.setClosable(false);
			TabContainer tc = tabType.createTab();
			tabs.add(tc);
			tab.setContent(tc);
			tabPane.getTabs().add(tab);
		}
		root.setCenter(tabPane);
		//
		stage.setTitle("SkidGUI " + VERSION);
		stage.setScene(new Scene(root, 666, 360));
		stage.show();
	}

	public static File getComparisonJar() {
		return jarComparison;
	}

	public static void setComparisonJar(File jarBase) {
		// Nothing needs to be notified at the moment that the comparison jar
		// has been loaded. Later it will need to be implemented.
		Main.jarComparison = jarBase;
	}

	public static File getTargetJar() {
		return jarTarget;
	}

	public static void setTargetJar(File jarTarget) {
		Main.jarTarget = jarTarget;
		// Notify the other tabs that the main jar has been loaded.

		for (TabContainer tabContainer : tabs) {
			if (tabContainer == null) {
				continue;
			}
			tabContainer.getTab().targetLoaded();
		}

	}
}
