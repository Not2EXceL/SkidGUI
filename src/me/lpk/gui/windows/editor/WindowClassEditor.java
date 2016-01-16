package me.lpk.gui.windows.editor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.optimizer.ClassConstantsCollector;
import org.objectweb.asm.optimizer.ClassOptimizer;
import org.objectweb.asm.optimizer.Constant;
import org.objectweb.asm.optimizer.ConstantPool;
import org.objectweb.asm.optimizer.Shrinker.ConstantComparator;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListView.EditEvent;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import me.lpk.asm.NothingVisitor;
import me.lpk.gui.Main;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.event.OLD_EDITOR.EditField;
import me.lpk.gui.event.editor.SaveEdited;
import me.lpk.gui.event.editor.SetClass;
import me.lpk.gui.event.editor.ShowConstants;
import me.lpk.gui.tabs.ExternalTab;
import me.lpk.mapping.MappingGen;
import me.lpk.mapping.modes.ModeNone;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.util.AlphabeticalComparator;
import me.lpk.util.JarUtil;

public class WindowClassEditor extends ExternalTab {
	private final Map<String, ClassNode> nodes = new TreeMap<String, ClassNode>(new AlphabeticalComparator());
	private final Map<String, MappedClass> remap = new TreeMap<String, MappedClass>(new AlphabeticalComparator());
	private ClassNode curNode;
	private Button btnChooseClass, btnConstants, btnFields, btnMethods, btnSave;

	@Override
	public void show() {
		Stage stage = new Stage();
		stage.setTitle("Class Editor");
		if (getScene() == null) {
			setup();
			stage.setScene(new Scene(this, 500, 300));
		} else {
			stage.setScene(this.getScene());
		}
		stage.show();
	}

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnChooseClass = new Button("Choose Class");
		btnConstants = new Button("Edit Strings");
		btnFields = new Button("Edit Fields");
		btnMethods = new Button("Edit Methods");
		btnSave = new Button("Save");
		btnConstants.setDisable(true);
		btnFields.setDisable(true);
		btnMethods.setDisable(true);
		btnSave.setDisable(true);
		//
		btnChooseClass.setOnAction(new SetClass(this));
		btnConstants.setOnAction(new ShowConstants(this));
		btnSave.setOnAction(new SaveEdited(this));
		//
		return new VerticalBar<Button>(1, btnChooseClass, btnConstants, btnFields, btnMethods,btnSave);
	}

	@Override
	protected BorderPane createOtherStuff() {
		BorderPane bp = new BorderPane();
		return bp;
	}

	@Override
	public void targetLoaded() {
		try {
			nodes.clear();
			nodes.putAll(JarUtil.loadClasses(Main.getTargetJar()));
			remap.clear();
			remap.putAll(MappingGen.getRename(new ModeNone(), nodes));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}

	public void setClass(ClassNode node) {
		btnConstants.setDisable(false);
		btnFields.setDisable(false);
		btnMethods.setDisable(false);
		btnSave.setDisable(false);
		//
		BorderPane bp = new BorderPane();
		VBox v1 = new VBox(7);
		v1.getChildren().add(new Label(" Class: " + node.name));
		v1.getChildren().add(new Label(" Super: " + node.superName));
		int childCount = 0;
		for (ClassNode other : nodes.values()) {
			if (other.superName.equals(node.name)) {
				childCount++;
			}
		}
		v1.getChildren().add(new Label(" Children: " + childCount));
		v1.getChildren().add(new Label(" Version: " + node.version));
		v1.getChildren().add(new Label(" SourceFile: " + node.sourceFile));
		v1.getChildren().add(new Label(" Fields: " + node.fields.size()));
		v1.getChildren().add(new Label(" Methods: " + node.methods.size()));
		bp.setCenter(v1);
		//
		curNode = node;
		otherControls = bp;
		update();
	}

	public void showConstants() {
		ConstantPool cp = new ConstantPool();
		ClassReader cr = null;
		try {
			cr = new ClassReader(curNode.name);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (cr == null) {
			JOptionPane.showMessageDialog(null, curNode.name + " could not be accessed!");
			return;
		}
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		ClassConstantsCollector ccc = new ClassConstantsCollector(cw, cp);
		cr.accept(ccc, ClassReader.SKIP_DEBUG);
		Set<Constant> constants = new TreeSet<Constant>(new ConstantComparator());
		constants.addAll(cp.values());
		BorderPane bp = new BorderPane();
		ListView<String> constList = new ListView<String>();
		constList.setEditable(true);
		constList.setCellFactory(TextFieldListCell.forListView());
		ObservableList<String> observablConstants = FXCollections.observableArrayList();
		for (Constant c : constants) {
			switch (c.type) {
			case 'S':
				observablConstants.add("" + c.strVal1);
				break;
			}

		}
		constList.setItems(observablConstants);
		constList.setOnEditCommit(new EventHandler<EditEvent<String>>() {
			@Override
			public void handle(EditEvent<String> event) {
				String s = constList.getItems().get(event.getIndex());
				
				Constant orig = cp.getOrDefault(s, null);//cp.get(s);
				if (orig != null) {
					Constant newConst = new Constant(orig);
					newConst.set('S', event.getNewValue(), null, null);
					cp.put(orig,newConst);
					
					ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
					ClassVisitor remapper = new ClassConstantsCollector(cw, cp);
					curNode.accept(remapper);
					constList.getItems().set(event.getIndex(), event.getNewValue());

				}else{
					System.out.println("FUCKING GOD DAMN");
				}
				
			}
		});
		bp.setCenter(constList);
		otherControls = bp;
		update();
	}

	public void save() {
		Map<String, byte[]> out = new HashMap<String, byte[]>();

		for (ClassNode cn : nodes.values()){
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			ClassVisitor cv = new NothingVisitor(cw);
			curNode.accept(cv);
			out.put(cn.name, cw.toByteArray());
		}
		
		
		JarUtil.saveAsJar(out, "FUCK.jar", false);
	}
}
