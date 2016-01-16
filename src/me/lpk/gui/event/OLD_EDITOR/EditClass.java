package me.lpk.gui.event.OLD_EDITOR;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import me.lpk.gui.tabs.TreeViewTab;
import me.lpk.mapping.objects.MappedClass;

public class EditClass implements ChangeListener<String> {
	private final TreeViewTab tab;
	private final String initVal;

	public EditClass(TreeViewTab tab, String initVal) {
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
