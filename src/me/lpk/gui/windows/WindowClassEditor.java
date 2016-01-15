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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.lpk.gui.Main;
import me.lpk.gui.controls.HorizontalBar;
import me.lpk.gui.controls.InternalWindow;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.event.editor.ChooseClass;
import me.lpk.gui.event.editor.EditClass;
import me.lpk.gui.tabs.ExternalTab;
import me.lpk.mapping.MappingGen;
import me.lpk.mapping.modes.ModeNone;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.util.JarUtil;

public class WindowClassEditor extends ExternalTab {
	private Map<String, MappedClass> remap = new HashMap<String, MappedClass>();
	private Map<String, InternalWindow> windows = new HashMap<String, InternalWindow>();
	private Map<String, ClassNode> nodes;
	private Pane containerThatHoldsWindows;
	private Button btnChoose;

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
			remap = MappingGen.getRename(new ModeNone(), nodes);
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
		//TODO: Set up so that the title and other things can be edited
		titleText.setEditable(false);
		titleText.setOnKeyPressed(new EditClass(this, titleText, node.name));
		// titleText.();
		Button btnClose = new Button("X");
		btnClose.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					InternalWindow win = windows.get(node.name);
					if (win != null) {
						containerThatHoldsWindows.getChildren().remove(win);
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
		InternalWindow interalWindow = new InternalWindow();
		interalWindow.setRoot(windowPane);
		interalWindow.makeDragable(title);
		interalWindow.makeDragable(titleText);
		interalWindow.makeResizable(20);
		interalWindow.makeFocusable();
		interalWindow.setMinWidth(title.getWidth());
		windows.put(node.name, interalWindow);
		containerThatHoldsWindows.getChildren().add(interalWindow);
	}

	private Node getBody(ClassNode node) {
		BorderPane bp = new BorderPane();
		VBox vbox = new VBox(node.methods.size() + node.fields.size());

		bp.setCenter(vbox);
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

	public Map<String, MappedClass> getRemaped() {
		return remap;
	}

	/**
	 * TODO: Update all references in open/future windows
	 * 
	 * @param initVal
	 * @param newValue
	 */
	public void updateClassName(String initVal, String newValue) {
		System.out.println(initVal + ":" + newValue);
		MappedClass mc = remap.get(initVal);
		if (mc != null) {
			mc.setRenamed(newValue);
		}
	}
}
