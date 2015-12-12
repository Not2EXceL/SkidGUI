package me.lpk.gui.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import me.lpk.event.gui.Obfuscate;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.mapping.MappingGen;

public class ObfuscationTab extends BasicTab {
	private ComboBox<String> cmbObfuscation;
	private TextField txtOutput;
	private static final String SIMPLE = "Simple", ABC = "Alphabetical", RAND = "Random-Short", UNI1="Unifucked";
	private Button btnReob, btnEditor;

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnReob = new Button("Reobfuscate jar");
		btnEditor = new Button("TODO: Other stuff");
		btnReob.setDisable(true);
		btnEditor.setDisable(true);
		//
		btnReob.setOnAction(new Obfuscate(this));
		//
		return new VerticalBar<Button>(1, btnReob, btnEditor);
	}

	@Override
	protected BorderPane createOtherStuff() {
		HBox hObfuType = new HBox(2);
		ObservableList<String> options = FXCollections.observableArrayList(SIMPLE, ABC, RAND,UNI1);
		cmbObfuscation = new ComboBox<String>(options);
		cmbObfuscation.setValue(SIMPLE);
		hObfuType.getChildren().add(new Label("Reobfuscation type:"));
		hObfuType.getChildren().add(cmbObfuscation);
		//
		HBox hExport = new HBox(2);
		txtOutput = new TextField("Obfuscated.jar");
		hExport.getChildren().add(new Label("Exported file name:"));
		hExport.getChildren().add(txtOutput);
		//
		VBox v = new VBox(2);
		v.getChildren().add(hObfuType);
		v.getChildren().add(hExport);
		return create(v);
	}

	@Override
	public void targetLoaded() {
		btnReob.setDisable(false);
		btnEditor.setDisable(false);
	}

	public int getObfuscation() {
		String type = cmbObfuscation.getValue().toString();
		switch (type) {
		case ABC:
			return MappingGen.ORDERED_DICTIONARY;
		case RAND:
			return MappingGen.RAND_DICTIONARY;
		case SIMPLE:
			return MappingGen.SIMPLE;
		case UNI1:
			return MappingGen.FUCKED;
		}
		return MappingGen.SIMPLE;
	}

	public String getExportedName() {
		return txtOutput.getText();
	}
}
