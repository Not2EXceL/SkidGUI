package me.lpk.gui.tabs;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ShowStage implements EventHandler<ActionEvent> {
	private final ExternalTab tab;

	public ShowStage(ExternalTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent event) {
		tab.show();
	}
}
