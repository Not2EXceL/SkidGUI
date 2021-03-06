package me.lpk.asm.stringrep;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.asm.MethodTransformer;
import me.lpk.util.OpUtil;

public class SimpleStringClassVisitor extends ClassVisitor {
	private final MethodTransformer trans;

	public SimpleStringClassVisitor(ClassVisitor cv, ClassNode node, String obClass, String obMethod) {
		super(OpUtil.ASM5, cv);
		trans = new SimpleStringTransformer(node, obClass, obMethod);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
		if (mv == null) {
			return null;
		}
		for (MethodNode mn : trans.getNode().methods) {
			if (mn.name.equals(name) && mn.desc.equals(desc)) {
				trans.transform(mn);
			}
		}
		return mv;
	}
}
