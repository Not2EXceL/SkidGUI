package me.lpk.gui.tabs;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.event.PatchJCrypt;
import me.lpk.gui.event.PatchSimpleStrings;
import me.lpk.gui.event.PatchZKM;

public class PatchingTab extends BasicTab {
	private Button btnDumpJ, btnFixZKM, btnCustom;
	private CheckBox chkInclusive, chkExclusive;
	private TextField txtInclude, txtExclude;
	private TextField txtClass, txtMethodName;

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnDumpJ = new Button("Patch JCrypt");
		btnFixZKM = new Button("Patch ZKM");
		btnCustom = new Button("Patch Custom");
		btnDumpJ.setDisable(true);
		btnFixZKM.setDisable(true);
		btnCustom.setDisable(true);
		//
		btnFixZKM.setOnAction(new PatchZKM(this));
		btnCustom.setOnAction(new PatchSimpleStrings(this));
		btnDumpJ.setOnAction(new PatchJCrypt());
		//
		return new VerticalBar<Button>(1, btnDumpJ, btnFixZKM, btnCustom);
	}

	@Override
	protected BorderPane createOtherStuff() {
		BorderPane bpChecks = new BorderPane();
		BorderPane bpInputs = new BorderPane();
		chkInclusive = new CheckBox("Limit to included packages");
		chkExclusive = new CheckBox("Limit to excluded packages");
		txtInclude = new TextField("Included packages");
		txtExclude = new TextField("Excluded packages");
		txtClass = new TextField("String obfu class");
		txtMethodName = new TextField("obfu method name");
		bpChecks.setRight(chkInclusive);
		bpChecks.setLeft(chkExclusive);
		bpInputs.setRight(txtInclude);
		bpInputs.setLeft(txtExclude);
		VBox v = new VBox(2);
		VBox vPackages = new VBox(2);
		vPackages.getChildren().add(bpChecks);
		vPackages.getChildren().add(bpInputs);
		BorderPane bpCustom2 = new BorderPane();
		bpCustom2.setCenter(new Label("Custom Obfuscation Patching"));
		VBox vCustom = new VBox(2);
		BorderPane bpCustom1 = new BorderPane();
		bpCustom1.setLeft(txtClass);
		bpCustom1.setRight(txtMethodName);
		vCustom.getChildren().add(bpCustom2);
		vCustom.getChildren().add(bpCustom1);
		v.getChildren().add(vPackages);
		v.getChildren().add(vCustom);
		return create(v);
	}

	@Override
	public void targetLoaded() {
		btnDumpJ.setDisable(false);
		btnFixZKM.setDisable(false);
		btnCustom.setDisable(false);
	}

	public String getObfuClass() {
		return txtClass.getText();
	}

	public String getObfuMethod() {
		return txtMethodName.getText();
	}
}
