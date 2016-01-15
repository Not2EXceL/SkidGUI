package me.lpk.gui.tabs;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.windows.WindowClassEditor;
import me.lpk.gui.windows.WindowObfuPatcher;
import me.lpk.gui.windows.WindowStringPatch;

public class PatchingTab extends BasicTab {
	private final WindowStringPatch stageStrings = new WindowStringPatch();
	private final WindowObfuPatcher stageObfusca = new WindowObfuPatcher();
	private final WindowClassEditor stageEditor = new WindowClassEditor();
	private Button btnObfuPatch, btnStringPatch, btnBytecode;

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnObfuPatch = new Button("Obfuscator Patcher");
		btnStringPatch = new Button("StringOb Patcher");
		btnBytecode = new Button("Edit Bytecode");
		btnObfuPatch.setDisable(true);
		btnStringPatch.setDisable(true);
		btnBytecode.setDisable(true);
		btnObfuPatch.setOnAction(new ShowStage(stageObfusca));
		btnStringPatch.setOnAction(new ShowStage(stageStrings));
		btnBytecode.setOnAction(new ShowStage(stageEditor));
		return new VerticalBar<Button>(1, btnObfuPatch, btnStringPatch, btnBytecode);
	}

	@Override
	protected BorderPane createOtherStuff() {
		BorderPane bp = new BorderPane();
		return create(bp);
	}

	@Override
	public void targetLoaded() {
		btnObfuPatch.setDisable(false);
		btnStringPatch.setDisable(false);
		btnBytecode.setDisable(false);
	}
}
