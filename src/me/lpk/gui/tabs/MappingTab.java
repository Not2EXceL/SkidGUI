package me.lpk.gui.tabs;

import javax.swing.JOptionPane;

import org.objectweb.asm.tree.ClassNode;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import me.lpk.gui.NodeEditor;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.event.SaveJar;
import me.lpk.gui.event.gui.TreeClick;

public class MappingTab extends TreeViewTab {
	private Button btnSaveJar, btnSaveMap, btnLoadMap;
	private NodeEditor nodeEditor;

	@Override
	protected VerticalBar<Control> createButtonList() {
		btnSaveJar = new Button("Save as jar");
		btnSaveMap = new Button("Save mappings");
		btnLoadMap = new Button("Load mappings");
		btnSaveJar.setDisable(true);
		btnSaveMap.setDisable(true);
		btnLoadMap.setDisable(true);
		btnSaveJar.setOnAction(new SaveJar(this));
		//
		return new VerticalBar<Control>(1, btnSaveJar, btnSaveMap);
	}

	@Override
	protected BorderPane createOtherStuff() {
		TreeItem<String> root = new TreeItem<String>("Load a jar");
		root.setExpanded(true);
		TreeItem<String> item = new TreeItem<String>("To see its contents");
		root.getChildren().add(item);
		tree = new TreeView<String>(root);
		//
		return create(tree);
	}

	@Override
	public EventHandler<MouseEvent> getClickEvent() {
		return new TreeClick(this);
	}

	@Override
	public void targetLoaded() {
		btnSaveJar.setDisable(false);
		btnSaveMap.setDisable(false);
		btnLoadMap.setDisable(false);
		super.targetLoaded();
	}

	public NodeEditor getNodeEditor() {
		return nodeEditor;
	}

	public void showNodeEditor(String key) {
		ClassNode node = nodes.get(key);
		if (node != null) {
			nodeEditor = new NodeEditor(this, node);
			otherControls = nodeEditor;
			update();
		} else {
			JOptionPane.showMessageDialog(null, "The class: " + key + " could not be found.");
		}
	}
}
