package me.lpk.gui;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.lpk.gui.controls.HorizontalBar;
import me.lpk.gui.event.OLD_EDITOR.EditClass;
import me.lpk.gui.event.OLD_EDITOR.EditField;
import me.lpk.gui.event.OLD_EDITOR.EditMethod;
import me.lpk.gui.event.OLD_EDITOR.GoBack;
import me.lpk.gui.tabs.MappingTab;

/**
 * TODO: Remove entierly.
 * 
 * The ideal editor will be like an interactive JD-GUI (Edit like a text
 * area[Within reason] and have modifications saved if syntax is ok)
 */
public class NodeEditor extends BorderPane {
	private static final int bigNum = 9999;
	private final ClassNode node;

	public NodeEditor(MappingTab tab, ClassNode node) {
		TextField txtClassName = new TextField(node.name);
		this.node = node;
		//
		// Control basics
		//
		HBox fieldsAndMethods = new HBox(2);
		ListView<String> fields = new ListView<String>();
		ListView<String> methods = new ListView<String>();
		fields.setPrefSize(bigNum, bigNum);
		methods.setPrefSize(bigNum, bigNum);
		fields.setEditable(true);
		methods.setEditable(true);
		fields.setCellFactory(TextFieldListCell.forListView());
		methods.setCellFactory(TextFieldListCell.forListView());
		//
		// Populating lists
		//
		ObservableList<String> observableFields = FXCollections.observableArrayList();
		ObservableList<String> observableMethods = FXCollections.observableArrayList();
		for (Object o : node.fields) {
			FieldNode fn = (FieldNode) o;
			observableFields.add(fn.name);
		}
		for (Object o : node.methods) {
			MethodNode mn = (MethodNode) o;
			if (shouldIgnore(mn.name)) {
				continue;
			}
			observableMethods.add(mn.name);
		}
		fields.setItems(observableFields);
		methods.setItems(observableMethods);
		fieldsAndMethods.getChildren().addAll(fields, methods);
		//
		// Event handling
		//
		txtClassName.textProperty().addListener(new EditClass(tab, node.name));
		fields.setOnEditCommit(new EditField(tab, fields));
		methods.setOnEditCommit(new EditMethod(tab, methods));
		//
		// Final formatting and packaging of the controls
		//
		VBox nameAndHbox = new VBox(2);
		Button btn = new Button("Back");
		btn.setOnAction(new GoBack(tab));
		nameAndHbox.getChildren().addAll(new HorizontalBar<Control>(0, txtClassName, btn), fieldsAndMethods);
		setCenter(nameAndHbox);
	}

	/**
	 * Checks if the method should be ignored.
	 * 
	 * @param name
	 * @return
	 */
	private boolean shouldIgnore(String name) {
		if (name.contains("<")) {
			return true;
		} else if (name.contains("$")) {
			return true;
		}
		return false;
	}

	public ClassNode getNode() {
		return node;
	}
}
