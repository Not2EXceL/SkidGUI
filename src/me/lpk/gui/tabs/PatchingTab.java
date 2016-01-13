package me.lpk.gui.tabs;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.stages.StageObfuPatcher;
import me.lpk.gui.stages.StageStringPatch;

public class PatchingTab extends BasicTab {
	private final StageStringPatch stageStrings = new StageStringPatch();
	private final StageObfuPatcher stageObfusca = new StageObfuPatcher();
	private Button btnObfuPatch, btnStringPatch;

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnObfuPatch = new Button("Obfuscator Patcher");
		btnStringPatch = new Button("StringOb Patcher");
		btnObfuPatch.setDisable(true);
		btnStringPatch.setDisable(true);
		btnObfuPatch.setOnAction(new ShowStage(stageObfusca));
		btnStringPatch.setOnAction(new ShowStage(stageStrings));
		return new VerticalBar<Button>(1, btnObfuPatch, btnStringPatch);
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
	}
}
