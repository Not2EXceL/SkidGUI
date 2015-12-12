package me.lpk.asm;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ClassRenamer extends ClassVisitor implements Opcodes {

	private Set<String> oldNames;
	private final String newName;

	public ClassRenamer(ClassVisitor cv, Set<String> oldNames, String newName) {
		super(ASM5, cv);
		this.oldNames = new HashSet<String>();
		this.oldNames.addAll(oldNames);
		this.newName = newName;
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		oldNames.add(name);
		cv.visit(version, ACC_PUBLIC, newName, signature, superName, interfaces);
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, fixDesc(desc), fix(signature), exceptions);
		if (mv != null && (access & ACC_ABSTRACT) == 0) {
			mv = new MethodRenamer(mv);
		}
		return mv;
	}

	class MethodRenamer extends MethodVisitor {
		public MethodRenamer(final MethodVisitor mv) {
			super(ASM4, mv);
		}

		public void visitTypeInsn(int i, String s) {
			if (oldNames.contains(s)) {
				s = newName;
			}
			mv.visitTypeInsn(i, s);
		}

		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			if (oldNames.contains(owner)) {
				mv.visitFieldInsn(opcode, newName, name, fix(desc));
			} else {
				mv.visitFieldInsn(opcode, owner, name, fix(desc));
			}
		}

		@SuppressWarnings("deprecation")
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			if (oldNames.contains(owner)) {
				mv.visitMethodInsn(opcode, newName, name, fix(desc));
			} else {
				mv.visitMethodInsn(opcode, owner, name, fix(desc));
			}
		}
	}

	private String fixDesc(String s) {
		return fix(s);
	}

	private String fix(String s) {
		if (s != null) {
			try {
				for (String name : oldNames) {
					if (s.indexOf(name) != -1) {
						s = replace(s, name, newName);
					}
				}
			} catch (Exception e) {
				System.out.println("Fix - " + s + " - " + e.getMessage());
			}
		}
		return s;
	}

	public String replace(String orig, String part1, String part2) {
		if (orig.contains(part1)){
			return orig;
		}
		StringBuffer sb = new StringBuffer(orig);
		sb.replace(sb.indexOf(part1), sb.indexOf(part1) + part1.length(), part2);
		return sb.toString();
	}
}