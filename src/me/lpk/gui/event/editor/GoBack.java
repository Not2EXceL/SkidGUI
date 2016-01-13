package me.lpk.gui.event.editor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.gui.tabs.MapTab;

public class GoBack implements EventHandler<ActionEvent> {
	private final MapTab tab;

	public GoBack(MapTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent event) {
		this.tab.goBack();
	}
}
