package me.lpk.asm.modify;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.util.OpCodes;

public class SimpleStringClassVisitor extends ClassVisitor {
	private final MethodTransformer trans;

	public SimpleStringClassVisitor(ClassVisitor cv, ClassNode node, String obClass, String obMethod) {
		super(OpCodes.ASM5, cv);
		trans = new SimpleStringTransformer(node, obClass, obMethod);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (mv == null) {
			return null;
		} else {
			mv = new SimpleStringMethodVisitor(mv);
			trans.setMethodVisitor(mv);
		}
		for (MethodNode mn : trans.getNode().methods) {
			if (mn.name.equals(name) && mn.desc.equals(desc)) {
				trans.transform(mn);
			}
		}
		return mv;
	}
}
