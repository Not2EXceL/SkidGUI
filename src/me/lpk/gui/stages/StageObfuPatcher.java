package me.lpk.gui.stages;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.event.patch.PatchJCrypt;
import me.lpk.gui.tabs.ExternalTab;

public class StageObfuPatcher extends ExternalTab {
	private Button btnDumpJ;

	@Override
	public void show() {
		Stage stage = new Stage();
		stage.setTitle("Obfuscation Patcher");
		setup();
		stage.setScene(new Scene(this, 500, 300));
		stage.show();
	}

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnDumpJ = new Button("Patch JCrypt");
		btnDumpJ.setOnAction(new PatchJCrypt());
		//
		return new VerticalBar<Button>(1, btnDumpJ);
	}

	@Override
	protected BorderPane createOtherStuff() {
		BorderPane bp = new BorderPane();
		return bp;
	}

	@Override
	public void targetLoaded() {

	}
}
