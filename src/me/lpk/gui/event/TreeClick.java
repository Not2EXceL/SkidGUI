package me.lpk.gui.event;

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import me.lpk.gui.tabs.MapTab;

public class TreeClick implements EventHandler<MouseEvent> {
	private final MapTab tab;

	public TreeClick(MapTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(MouseEvent mouseEvent) {
		if (mouseEvent.getClickCount() == 2) {
			TreeItem<String> item = tab.getSelected();
			if (item.getChildren().size() > 0) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			while (item.getParent() != null) {
				sb.insert(0,"/" + item.getValue());
				item = item.getParent();
			}
			String selected = sb.substring(1);
			if (tab.hasNode(selected)){
				tab.showNodeEditor(selected);
			}
		}
	}
}
