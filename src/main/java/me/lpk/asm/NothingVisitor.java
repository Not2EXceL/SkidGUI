package me.lpk.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class NothingVisitor extends ClassVisitor {

	public NothingVisitor(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

}
