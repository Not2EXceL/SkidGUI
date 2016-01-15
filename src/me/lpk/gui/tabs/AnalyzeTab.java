package me.lpk.gui.tabs;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import me.lpk.gui.controls.VerticalBar;
import me.lpk.gui.event.Analyze;
import me.lpk.gui.windows.WindowObfuPatcher;
import me.lpk.gui.windows.WindowStringPatch;

public class AnalyzeTab extends BasicTab {
	private Button btnIP, btnURL, btnBytecode;

	@Override
	protected VerticalBar<Button> createButtonList() {
		btnIP = new Button("Dump IPs");
		btnURL = new Button("Dump URLs");
		btnBytecode = new Button("Suspicion Report");
		btnIP.setDisable(true);
		btnURL.setDisable(true);
		btnBytecode.setDisable(true);
		btnIP.setOnAction(new Analyze(Analyze.DUMP_IP));
		btnURL.setOnAction(new Analyze(Analyze.DUMP_URL));
		btnBytecode.setOnAction(new Analyze(Analyze.SUSP_REP));
		return new VerticalBar<Button>(1, btnIP, btnURL,btnBytecode);
	}

	@Override
	protected BorderPane createOtherStuff() {
		BorderPane bp = new BorderPane();
		//Package include / discludue
		//Make it one of the two colum things and you drag over to one side what packages you want to ignore
		return create(bp);
	}

	@Override
	public void targetLoaded() {
		btnIP.setDisable(false);
		btnURL.setDisable(false);
		btnBytecode.setDisable(false);
	}
}
