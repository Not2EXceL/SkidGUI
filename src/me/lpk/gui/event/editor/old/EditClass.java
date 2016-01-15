package me.lpk.gui.event.editor.old;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import me.lpk.gui.tabs.MappingTab;
import me.lpk.mapping.objects.MappedClass;

public class EditClass implements ChangeListener<String> {
	private final MappingTab tab;
	private final String initVal;

	public EditClass(MappingTab tab, String initVal) {
		this.initVal = initVal;
		this.tab = tab;
	}

	@Override
	public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		if (newValue.endsWith("/") || newValue.startsWith("/")) {
			return;
		}
		MappedClass mc = tab.getRemap().get(initVal);
		if (mc != null) {
			mc.setRenamed(newValue);
		}
	}
}
