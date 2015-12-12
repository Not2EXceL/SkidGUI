package me.lpk.gui.tabs;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import me.lpk.event.gui.PatchJCrypt;
import me.lpk.event.gui.PatchZKM;
import me.lpk.gui.controls.VerticalBar;

public class PatchingTab extends BasicTab {
	private Button btnDumpJ, btnFixZKM;
	private CheckBox chkInclusive, chkExclusive;
	private TextField txtInclude, txtExclude;

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnDumpJ = new Button("Patch JCrypt");
		btnFixZKM = new Button("Patch ZKM");
		btnDumpJ.setDisable(true);
		btnFixZKM.setDisable(true);
		//
		btnFixZKM.setOnAction(new PatchZKM(this));
		btnDumpJ.setOnAction(new PatchJCrypt());
		//
		return new VerticalBar<Button>(1, btnDumpJ, btnFixZKM);
	}

	@Override
	protected BorderPane createOtherStuff() {
		BorderPane bpChecks = new BorderPane();
		BorderPane bpInputs = new BorderPane();
		chkInclusive = new CheckBox("Limit to included packages");
		chkExclusive = new CheckBox("Limit to excluded packages");
		txtInclude = new TextField("Included packages");
		txtExclude = new TextField("Excluded packages");
		bpChecks.setRight(chkInclusive);
		bpChecks.setLeft(chkExclusive);
		bpInputs.setRight(txtInclude);
		bpInputs.setLeft(txtExclude);
		VBox v = new VBox(2);
		v.getChildren().add(bpChecks);
		v.getChildren().add(bpInputs);
		return create(v);
	}

	@Override
	public void targetLoaded() {
		btnDumpJ.setDisable(false);
		btnFixZKM.setDisable(false);
	}
}
