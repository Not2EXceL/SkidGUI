package me.lpk.asm.modify;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class SimpleStringTransformer extends MethodTransformer {
	public final static String STRING_IN_OUT = "(Ljava/lang/String;)Ljava/lang/String;";
	private final String obClass, obMethod;

	public SimpleStringTransformer(ClassNode node, String obClass, String obMethod) {
		super(node);
		this.obClass = obClass;
		this.obMethod = obMethod;
	}

	@Override
	public void setMethodVisitor(MethodVisitor mv) {
		if (mv instanceof SimpleStringMethodVisitor) {
			SimpleStringMethodVisitor ssf = (SimpleStringMethodVisitor) mv;
			ssf.setRemoveName(obMethod);
			ssf.setRemoveOwner(obClass);
			super.setMethodVisitor(mv);
		} else {
			super.setMethodVisitor(null);
		}
	}

	@Override
	public void transform(MethodNode method) {
		if (mv == null) {
			return;
		}
		Iterator<AbstractInsnNode> i = method.instructions.iterator();
		Set<ObfuCall> locs = new HashSet<ObfuCall>();
		int index = -1;
		while (i.hasNext()) {
			index++;
			AbstractInsnNode ain = i.next();
			if (ain == null) {
				continue;
			}
			AbstractInsnNode prev = ain.getPrevious();
			if (prev == null) {
				continue;
			}
			if (ain instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) ain;
				if (min.getOpcode() == Opcodes.INVOKESTATIC && min.desc.equals(STRING_IN_OUT) && min.getPrevious().getOpcode() == Opcodes.LDC) {
					LdcInsnNode ldc = (LdcInsnNode) min.getPrevious();
					locs.add(new ObfuCall(min, index, ldc));
				}
			}
		}
		if (locs.size() > 0) {
			for (ObfuCall loc : locs) {
				SimpleStringMethodVisitor ssf = (SimpleStringMethodVisitor) mv;
				ssf.setInput(loc.input.cst.toString());
				// For some reason I don't need to 'accept' the method visitor
				// for it to run... I don't know how it works but it does.
				
				 //loc.input.accept(ssf); 
				 //loc.min.accept(ssf);
				 
				System.out.println("\tReplacing obfuscation call at index: " + loc.invokeIndex + "("+ method.owner.name + "." + method.name + ")");
			}
		}
	}

	class ObfuCall {
		MethodInsnNode min;
		int invokeIndex;
		LdcInsnNode input;

		ObfuCall(MethodInsnNode min, int invokeIndex, LdcInsnNode input) {
			this.min = min;
			this.invokeIndex = invokeIndex;
			this.input = input;
		}
	}
}