package me.lpk.gui.event.editor.old;

import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;
import me.lpk.gui.tabs.MappingTab;
import me.lpk.mapping.objects.MappedMethod;

public class EditMethod implements EventHandler<ListView.EditEvent<String>> {
	private final MappingTab tab;
	private final ListView<String> methods;

	public EditMethod(MappingTab tab, ListView<String> methods) {
		this.methods = methods;
		this.tab = tab;
	}

	@Override
	public void handle(EditEvent<String> event) {
		String nodeName = tab.getNodeEditor().getNode().name;
		String methodName = methods.getItems().get(event.getIndex());
		MappedMethod mm = tab.getRemap().get(nodeName).getMethods().get(methodName);
		if (mm != null) {
			mm.setRenamed(event.getNewValue());
		}
		methods.getItems().set(event.getIndex(), event.getNewValue());
	}
}
