package me.lpk.asm.zkm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.asm.MethodTransformer;
import me.lpk.util.OpUtil;

public class ZKMClassVisitor extends ClassVisitor {
	private final MethodTransformer trans;

	public ZKMClassVisitor(ClassVisitor cv, ClassNode node) {
		super(OpUtil.ASM5, cv);
		trans = new ZKMMethodTransformer(node);
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
