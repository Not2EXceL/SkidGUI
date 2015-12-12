package me.lpk.event.gui.editor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import me.lpk.gui.tabs.MapTab;
import me.lpk.mapping.objects.MappedClass;

public class EditClass implements ChangeListener<String> {
	private final MapTab tab;
	private final String initVal;

	public EditClass(MapTab tab, String initVal) {
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
