package me.lpk.gui.windows;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.event.patch.PatchJCrypt;
import me.lpk.gui.event.patch.PatchZKM;
import me.lpk.gui.tabs.ExternalTab;

public class WindowObfuPatcher extends ExternalTab {
	private Button btnDumpJ, btnPatchZKM;

	@Override
	public void show() {
		Stage stage = new Stage();
		stage.setTitle("Obfuscation Patcher");
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
		btnDumpJ = new Button("Patch JCrypt");
		btnPatchZKM = new Button("Patch ZKM");
		btnDumpJ.setOnAction(new PatchJCrypt());
		btnPatchZKM.setOnAction(new PatchZKM());
		//
		return new VerticalBar<Button>(1, btnDumpJ,btnPatchZKM);
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
