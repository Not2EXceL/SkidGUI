package me.lpk.mapping.objects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;

public class MappedClass extends MappedObject {
	private final Map<String, MappedField> fields = new HashMap<String, MappedField>();
	private final Map<String, MappedMethod> methods = new HashMap<String, MappedMethod>();
	private final Set<MappedClass> children = new HashSet<MappedClass>();
	private final ClassNode node;
	private MappedClass parent;

	public MappedClass(ClassNode node, String renamed, MappedClass parent) {
		super(node.name, renamed);
		this.node = node;
		this.parent = parent;
	}

	public Map<String, MappedField> getFields() {
		return fields;
	}

	public Map<String, MappedMethod> getMethods() {
		return methods;
	}

	public Set<MappedClass> getChildren() {
		return children;
	}

	public void addChild(MappedClass mappedClass) {
		children.add(mappedClass);
	}

	public MappedClass getParent() {
		return parent;
	}

	public ClassNode getNode() {
		return node;
	}

	public void setParent(MappedClass parent) {
		this.parent = parent;
	}
}
