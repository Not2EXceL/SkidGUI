package me.lpk.gui.windows;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import me.lpk.gui.controls.HorizontalBar;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.event.patch.PatchSimpleStrings;
import me.lpk.gui.tabs.ExternalTab;

public class WindowStringPatch extends ExternalTab {
	private TextField txtClass, txtMethodName;
	private Button btnDecrypt;

	@Override
	public void show() {
		Stage stage = new Stage();
		stage.setTitle("Simple String Decryption");
		if (getScene() == null) {
			setup();
			stage.setScene(new Scene(this, 620, 200));
		} else {
			stage.setScene(this.getScene());
		}
		stage.show();
	}

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnDecrypt = new Button("Decrypt");
		btnDecrypt.setOnAction(new PatchSimpleStrings(this));
		return new VerticalBar<Button>(1, btnDecrypt);
	}

	@Override
	protected BorderPane createOtherStuff() {
		txtClass = new TextField("example/ObfuClass");
		txtMethodName = new TextField("obfuMethodName");
		HorizontalBar<TextField> h = new HorizontalBar<TextField>(1, txtClass, txtMethodName);
		BorderPane bpObfuInfo = new BorderPane();
		bpObfuInfo.setCenter(h);
		return bpObfuInfo;

	}

	public String getOClass() {
		return txtClass.getText();
	}

	public String getOMethod() {
		return txtMethodName.getText();
	}

	@Override
	public void targetLoaded() {
	}
}
