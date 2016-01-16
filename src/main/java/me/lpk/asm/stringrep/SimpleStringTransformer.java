package me.lpk.asm.stringrep;

import java.lang.reflect.InvocationTargetException;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.asm.MethodTransformer;

public class SimpleStringTransformer extends MethodTransformer {
	public final static String STRING_OUT = ")Ljava/lang/String;";
	private final String obClass, obMethod;

	public SimpleStringTransformer(ClassNode node, String obClass, String obMethod) {
		super(node);
		this.obClass = obClass;
		this.obMethod = obMethod;
	}

	@Override
	public void transform(MethodNode method) {
		for (AbstractInsnNode ain : method.instructions.toArray()) {
			if (ain instanceof MethodInsnNode) {
				MethodInsnNode min = (MethodInsnNode) ain;
				Class<?> param = getParam(min.desc);
				if (min.getOpcode() == Opcodes.INVOKESTATIC && min.owner.contains(obClass) && min.name.equals(obMethod) && min.desc.endsWith(STRING_OUT) && param != null) {
					int opcode = min.getPrevious().getOpcode();
					if (opcode == Opcodes.LDC) {
						LdcInsnNode ldc = (LdcInsnNode) min.getPrevious();
						method.instructions.remove(min);
						ldc.cst = getValue(min.owner, min.name, param, ldc.cst);
					} else if (opcode == Opcodes.ICONST_0 || opcode == Opcodes.ICONST_1 || opcode == Opcodes.ICONST_2 || opcode == Opcodes.ICONST_3 || opcode == Opcodes.ICONST_4 || opcode == Opcodes.ICONST_5) {
						InsnNode in = (InsnNode) min.getPrevious();
						int inVal = opcode == Opcodes.ICONST_0 ? 0 : opcode == Opcodes.ICONST_1 ? 1 : opcode == Opcodes.ICONST_2 ? 2 : opcode == Opcodes.ICONST_3 ? 3 : opcode == Opcodes.ICONST_4 ? 4 : opcode == Opcodes.ICONST_5 ? 5 : -1;
						if (inVal >= 0) {
							method.instructions.remove(min);
							method.instructions.set(in, new LdcInsnNode(getValue(min.owner, min.name, param, inVal)));
						}
					} else if (opcode == Opcodes.SIPUSH || opcode == Opcodes.BIPUSH) {
						IntInsnNode iin = (IntInsnNode) min.getPrevious();
						method.instructions.remove(min);
						method.instructions.set(iin, new LdcInsnNode(getValue(min.owner, min.name, param, iin.operand)));
					}
				}
			}
		}

	}

	private Class<?> getParam(String desc) {
		if (desc.contains(")") && desc.length() > 2) {
			int end = desc.indexOf(";)");
			// If the index is -1, input is a primitive
			if (end == -1) {
				if (desc.startsWith("(I)")) {
					// Int
					return int.class;
				} else if (desc.startsWith("(J)")) {
					// Long
					return long.class;
				}
				// I doubt anyone would use double or float. So no support for
				// those needed.
				return null;
			}
			String first = desc.substring(2, end);
			if (!first.contains(";")) {
				try {
					return Class.forName(first.replace("/", "."));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * TODO: Using ASM copy out the method into an empty class. Then use
	 * reflection on the empty class. Will possibly prevent malicious code from
	 * executing in <clinit>
	 * 
	 * @param owner
	 * @param name
	 * @param in
	 * @return
	 */
	private Object getValue(String owner, String name, Class<?> param, Object in) {
		try {
			return Class.forName(owner.replace("/", ".")).getDeclaredMethod(name, param).invoke(null, in);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return "FAILED_GET_VALUE";
	}
}