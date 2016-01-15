package me.lpk.gui.windows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.objectweb.asm.tree.ClassNode;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import me.lpk.gui.Main;
import me.lpk.gui.controls.HorizontalBar;
import me.lpk.gui.controls.InternalWindow;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.event.editor.ChooseClass;
import me.lpk.gui.event.patch.PatchSimpleStrings;
import me.lpk.gui.tabs.ExternalTab;
import me.lpk.util.JarUtil;

public class WindowClassEditor extends ExternalTab {
	private Map<String, InternalWindow> windows = new HashMap<String, InternalWindow>();
	private Map<String, ClassNode> nodes;
	private Pane containerThatHoldsWindows;
	private Button btnChoose;
	private int windex;

	@Override
	public void show() {
		Stage stage = new Stage();
		stage.setTitle("Class editor");
		if (getScene() == null) {
			stage.setScene(new Scene(this, 800, 600));
			setup();
		} else {
			stage.setScene(this.getScene());
		}
		stage.show();
		try {
			nodes = JarUtil.loadClasses(Main.getTargetJar());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addWindow(ClassNode node) {
		if (node == null) {
			JOptionPane.showMessageDialog(null, "The specified node could not be found!");
			return;
		} else if (windows.containsKey(node.name)) {
			JOptionPane.showMessageDialog(null, "That node is already open!");
			return;
		}
		TextField titleText = new TextField(node.name);
		Button btnClose = new Button("X");
		btnClose.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					InternalWindow win = windows.get(node.name);
					if (win != null) {
						containerThatHoldsWindows.getChildren().remove(win.index);
						windows.remove(node.name);
					} else {
						System.err.println(node.name + " COULD NOT BE FOUND");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		HorizontalBar<Control> title;
		if (node.superName.equals("java/lang/Object")) {
			title = new HorizontalBar<Control>(1, titleText, btnClose);
		} else {
			Button btnParent = new Button("Open parent");
			btnParent.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if (nodes.containsKey(node.superName)) {
						addWindow(nodes.get(node.superName));
					}
				}
			});
			title = new HorizontalBar<Control>(1, titleText, btnParent, btnClose);
		}

		BorderPane windowPane = new BorderPane();
		try {
			windowPane.setStyle("-fx-border-width: 1; -fx-border-color: black;");
			title.setStyle("-fx-border-width: 1; -fx-border-color: black; -fx-color: grey;");
		} catch (Exception e) {
			e.printStackTrace();
		}
		windowPane.setTop(title);
		windowPane.setCenter(getBody(node));
		InternalWindow interalWindow = new InternalWindow(windex++);
		interalWindow.setRoot(windowPane);
		interalWindow.makeDragable(title);
		interalWindow.makeDragable(titleText);
		interalWindow.makeResizable(20);
		interalWindow.makeFocusable();
		windows.put(node.name, interalWindow);
		containerThatHoldsWindows.getChildren().add(interalWindow);
	}

	private Node getBody(ClassNode node) {
		BorderPane bp = new BorderPane();
		bp.setMinSize(200, 100);
		return bp;
	}

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnChoose = new Button("Choose Class");
		btnChoose.setOnAction(new ChooseClass(this));
		return new VerticalBar<Button>(1, btnChoose);
	}

	@Override
	protected BorderPane createOtherStuff() {
		BorderPane bpObfuInfo = new BorderPane();
		containerThatHoldsWindows = new Pane();
		bpObfuInfo.setCenter(containerThatHoldsWindows);
		return bpObfuInfo;
	}

	@Override
	public void targetLoaded() {
	}
}
