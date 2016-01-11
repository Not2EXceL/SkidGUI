package me.lpk.asm.modify;

import java.lang.reflect.InvocationTargetException;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import me.lpk.util.OpCodes;

public class SimpleStringMethodVisitor extends MethodVisitor {
	private String remName, remOwner, input;

	public SimpleStringMethodVisitor(MethodVisitor mv) {
		super(Opcodes.ASM5, mv);
	}

	public void setRemoveName(String toRem) {
		this.remName = toRem;
	}

	public void setRemoveOwner(String remOwner) {
		this.remOwner = remOwner;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public boolean canRun() {
		return (remName != null && remOwner != null && input != null);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		if (!cst.equals(input)) {
			super.visitLdcInsn(cst);
		}
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		if (canRun()) {
			if (desc.equals(SimpleStringTransformer.STRING_IN_OUT)) {
				String o = owner.contains("/") ? owner.substring(owner.lastIndexOf("/") + 1) : owner;
				System.out.println("\t\t" + OpCodes.opcodes.get(opcode) + ": " + o + "." + name + "(\"" + input + "\");");
				String out = getValue(owner, name, input);
				super.visitLdcInsn(out);
			} else {
				super.visitMethodInsn(opcode, owner, name, desc, itf);
			}
		} else {
			super.visitMethodInsn(opcode, owner, name, desc, itf);
		}
	}

	private String getValue(String owner, String name, String in) {
		try {
			return (String) Class.forName(owner.replace("/", ".")).getDeclaredMethod(name, String.class).invoke(null, in);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return "FAILED_GETVALIE";
	}
}
