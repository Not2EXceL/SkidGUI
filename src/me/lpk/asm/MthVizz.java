package me.lpk.asm;

import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.commons.InstructionAdapter;
import org.objectweb.asm.optimizer.ConstantPool;
import org.objectweb.asm.optimizer.MethodConstantsCollector;

import me.lpk.mapping.objects.MappedClass;
import me.lpk.mapping.objects.MappedMethod;
import me.lpk.util.OpCodes;
import me.lpk.util.StringUtil;

public class MthVizz extends MethodConstantsCollector {
	private final Map<String, MappedClass> renamemap;
	private final String name;

	public MthVizz(String name, ConstantPool cp, Map<String, MappedClass> renamemap, MethodVisitor mv) {
		super(mv, cp);
		this.name = name;
		this.renamemap = renamemap;
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		MappedClass mc = renamemap.get(owner);
		if (mc != null && mc.getFields().containsKey(name)) {
			name = mc.getFields().get(name).getRenamed();
		}
		owner = StringUtil.fixDesc(owner, renamemap.values());
		desc = StringUtil.fixDesc(desc, renamemap.values());
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		desc = StringUtil.fixDesc(desc, renamemap.values());
		signature = StringUtil.fixDesc(signature, renamemap.values());
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

	@Override
	public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
		MappedClass mc = StringUtil.getMappedFromDesc(renamemap, desc);
		if (mc != null) {

			MappedMethod mm = mc.getMethods().get(name);
			if (mm != null) {
				System.out.println(name + "-" + mm.getRenamed());
				name = mm.getRenamed();
			}
		}
		desc = StringUtil.fixDesc(desc, renamemap.values());
		String owner2 = StringUtil.fixDesc(bsm.getOwner(), renamemap.values());
		String name2 = name;
		String desc2 = StringUtil.fixDesc(bsm.getDesc(), renamemap.values());
		bsm = new Handle(bsm.getTag(), owner2, name2, desc2);
		super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		visitMethodInsn(opcode, owner, name, desc, false);
		System.out.println("\t\tDeprecated call to visitMethodInsn");
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		name = updateMethodName(name, owner);
		owner = StringUtil.fixDesc(owner, renamemap.values());
		desc = StringUtil.fixDesc(desc, renamemap.values());
		super.visitMethodInsn(opcode, owner, name, desc, itf);
	}

	@Override
	public void visitTypeInsn(int opcode, String type) {
		type = StringUtil.fixDesc(type, renamemap.values());
		super.visitTypeInsn(opcode, type);
	}

	@Override
	public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
		int in = 0;
		for (Object o : local) {
			if (o == null) {
				continue;
			} else if (o instanceof String) {
				o = StringUtil.fixDesc(o.toString(), renamemap.values());
			}
			local[in] = o;
			in++;
		}
		in = 0;
		for (Object o : stack) {
			if (o == null) {
				continue;
			} else if (o instanceof String) {
				o = StringUtil.fixDesc(o.toString(), renamemap.values());
			}
			stack[in] = o;
			in++;
		}
		super.visitFrame(type, nLocal, local, nStack, stack);
	}

	@Override
	public void visitAttribute(Attribute attr) {
		super.visitAttribute(attr);
	}

	@Override
	public void visitParameter(String name, int access) {
		super.visitParameter(name, access);
	}

	@Override
	public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		return super.visitInsnAnnotation(typeRef, typePath, desc, visible);
	}

	@Override
	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		super.visitTryCatchBlock(start, end, handler, type);
	}

	@Override
	public void visitLdcInsn(Object cst) {
		super.visitLdcInsn(cst);
	}

	private String updateMethodName(String name, String owner) {
		// Get the method's MappedClass by its owner
		MappedClass mc = renamemap.get(owner);
		if (mc != null) {
			// If the method is right there, we're done!
			MappedMethod mm = mc.getMethods().get(name);
			if (mm != null) {
				name = mm.getRenamed();
			} else {
				// Otherwise if the owner is itself but the method is in a
				// parent class, iterate through parents
				MappedClass mc2 = mc.getParent();
				while (mc2 != null) {
					MappedMethod mm2 = mc2.getMethods().get(name);
					if (mm2 != null) {
						name = mm2.getRenamed();
					}
					mc2 = mc2.getParent();
				}
			}
		}
		return name;
	}
}
