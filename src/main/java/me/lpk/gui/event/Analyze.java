package me.lpk.gui.event;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.gui.Main;

public class Analyze implements EventHandler<ActionEvent> {
	public static final int DUMP_IP = 1, DUMP_URL = 2, SUSP_REP = 0;
	public int mode;

	public Analyze(int mode) {
		this.mode = mode;
	}

	@Override
	public void handle(ActionEvent event) {
		List<String> lines = ((mode == DUMP_IP) ? dumpIP() : ((mode == DUMP_URL) ? dumpURL() : ((mode == SUSP_REP) ? suspReport() : null)));
		if (lines != null) {
			try {
				FileUtils.writeLines(new File(Main.getTargetJar().getName() + "-Report.txt"), lines);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private List<String> suspReport() {
		List<String> list = new ArrayList<String>();
		return list;
	}

	private List<String> dumpURL() {
		List<String> list = new ArrayList<String>();
		return list;
	}

	private List<String> dumpIP() {
		List<String> list = new ArrayList<String>();
		return list;
	}

}
