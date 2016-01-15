package me.lpk.gui.event.editor;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import me.lpk.gui.windows.WindowClassEditor;
import me.lpk.mapping.objects.MappedClass;

public class EditClass implements EventHandler<KeyEvent> {
	private final WindowClassEditor tab;
	private final TextField text;
	private final String initVal;

	public EditClass(WindowClassEditor tab, TextField text, String initVal) {
		this.initVal = initVal;
		this.text = text;
		this.tab = tab;
	}

	@Override
	public void handle(KeyEvent event) {
		if (!event.getCode().name().equals("ENTER")){
			return;
		}
		String newValue = text.getText();
		if (newValue.endsWith("/") || newValue.startsWith("/")) {
			return;
		}
		tab.updateClassName(initVal, newValue);
		
	}
}
