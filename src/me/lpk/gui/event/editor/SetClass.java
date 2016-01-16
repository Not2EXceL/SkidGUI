package me.lpk.gui.event.editor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.tabs.TreeViewTab;
import me.lpk.gui.windows.editor.WindowClassEditor;

/**
 * This is really ugly but it doesn't deserve multiple classes.
 */
public class SetClass implements EventHandler<ActionEvent> {
	private final WindowClassEditor editor;
	private Stage stge;
	private StageSelection sel;

	public SetClass(WindowClassEditor editor) {
		this.editor = editor;
	}

	@Override
	public void handle(ActionEvent event) {
		try {
			sel = new StageSelection();
			sel.show();
			sel.targetLoaded();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The window that opens prompting the user to choose a class. They can
	 * navigate through the jar and double click an entry or choose one and hit
	 * "select".
	 */
	private class StageSelection extends TreeViewTab {

		@Override
		public EventHandler<MouseEvent> getClickEvent() {
			return new InternalChooseTree();
		}

		@Override
		protected VerticalBar<Button> createButtonList() {
			Button btn = new Button("Select");
			btn.setOnAction(new InternalChooseButton());
			return new VerticalBar<Button>(1, btn);
		}

		public void show() {
			Stage stage = new Stage();
			stge = stage;
			stage.setTitle("Choose a class");
			setup();
			stage.setScene(new Scene(this, 500, 300));
			stage.show();
		}
	}

	/**
	 * Choose event handler for the Button.
	 * 
	 * I really with the JVM type-erasure would allow E<A>, E<B>... Alas I have
	 * to make two classes now.
	 */
	private class InternalChooseButton implements EventHandler<ActionEvent> {
		@Override
		public void handle(ActionEvent event) {
			tryAddNodeWindow();
		}
	}

	/**
	 * Choose event handler for the TreeView.
	 */
	private class InternalChooseTree implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			if (event.getClickCount() == 2) {
				tryAddNodeWindow();
			}
		}
	}

	/**
	 * Create a node window in the editor if the node is selected is valid.
	 */
	private void tryAddNodeWindow() {
		TreeItem<String> item = sel.getSelected();
		if (item == null || item.getChildren().size() > 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		while (item.getParent() != null) {
			sb.insert(0, "/" + item.getValue());
			item = item.getParent();
		}
		String selected = sb.substring(1);
		if (sel.hasNode(selected)) {
			editor.setClass(sel.getNodes().get(selected));
			stge.close();
		}
	}
}
