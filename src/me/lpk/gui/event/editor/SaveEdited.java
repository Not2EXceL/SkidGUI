package me.lpk.gui.event.editor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.gui.windows.editor.WindowClassEditor;

public class SaveEdited implements EventHandler<ActionEvent> {

	private final WindowClassEditor wce;

	public SaveEdited(WindowClassEditor wce) {
		this.wce = wce;
	}

	@Override
	public void handle(ActionEvent event) {
		wce.save();
	}

}
