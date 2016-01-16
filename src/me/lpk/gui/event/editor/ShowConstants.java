package me.lpk.gui.event.editor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.gui.windows.editor.WindowClassEditor;

public class ShowConstants implements EventHandler<ActionEvent> {
	private final WindowClassEditor wce;

	public ShowConstants(WindowClassEditor wce) {
		this.wce = wce;
	}

	@Override
	public void handle(ActionEvent event) {
		wce.showConstants();
	}

}
