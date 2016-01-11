package me.lpk.gui.event.editor;

import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;
import me.lpk.gui.tabs.MapTab;
import me.lpk.mapping.objects.MappedField;

public class EditField implements EventHandler<ListView.EditEvent<String>> {
	private final MapTab tab;
	private final ListView<String> fields;

	public EditField(MapTab tab, ListView<String> fields) {
		this.fields = fields;
		this.tab = tab;
	}

	@Override
	public void handle(EditEvent<String> event) {
		String nodeName =tab.getNodeEditor().getNode().name;
		String methodName = fields.getItems().get(event.getIndex());
		MappedField mf = tab.getRemap().get(nodeName).getFields().get(methodName);
		if (mf != null) {
			mf.setRenamed(event.getNewValue());
		}
		fields.getItems().set(event.getIndex(), event.getNewValue());
	}
}
