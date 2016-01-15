package me.lpk.gui.event.editor.old;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.gui.tabs.MappingTab;

public class GoBack implements EventHandler<ActionEvent> {
	private final MappingTab tab;

	public GoBack(MappingTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent event) {
		this.tab.goBack();
	}
}
