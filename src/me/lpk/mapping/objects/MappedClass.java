package me.lpk.mapping.objects;

import java.util.HashMap;
import java.util.Map;

public class MappedClass extends MappedObject {
	private final Map<String, MappedField> fields = new HashMap<String, MappedField>();
	private final Map<String, MappedMethod> methods = new HashMap<String, MappedMethod>();
	private final MappedClass parent;

	public MappedClass(String original, String renamed, MappedClass parent) {
		super(original, renamed);
		this.parent = parent;
	}

	public Map<String, MappedField> getFields() {
		return fields;
	}

	public Map<String, MappedMethod> getMethods() {
		return methods;
	}

	public MappedClass getParent() {
		return parent;
	}
}
