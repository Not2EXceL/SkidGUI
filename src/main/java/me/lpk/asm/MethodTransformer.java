package me.lpk.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class MethodTransformer {
	protected final ClassNode node;

	public MethodTransformer(ClassNode node) {
		this.node = node;
	}

	public abstract void transform(MethodNode method);

	public final ClassNode getNode() {
		return node;
	}
}