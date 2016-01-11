package me.lpk.gui.event;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.gui.tabs.PatchingTab;

public class PatchZKM implements EventHandler<ActionEvent> {
	//I hate warnings. Will eventually be used... 
	@SuppressWarnings("unused")
	private final PatchingTab tab;

	public PatchZKM(PatchingTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent e) {

		// There was a reference to an old build JMD here, but it was long, kind
		// of ugly and I had no clue how it worked.
		//
		// General plan for patching ZKM string protected jars:
		//
		// TODO: Map the values in string[] or string generated in the <init>
		// method
		// TODO: Find the instruction set for loading strings from the array and
		// replace correct string
		// TODO: Export jar
	}
}
