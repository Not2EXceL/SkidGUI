package me.lpk.asm;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.optimizer.ClassConstantsCollector;
import org.objectweb.asm.optimizer.Constant;
import org.objectweb.asm.optimizer.ConstantPool;

import me.lpk.mapping.objects.MappedClass;
import me.lpk.mapping.objects.MappedField;
import me.lpk.mapping.objects.MappedMethod;
import me.lpk.util.OpCodes;
import me.lpk.util.StringUtil;

//https://dzone.com/articles/decrypting-java-malware-using - Decrypting static calls 
public class ClzzVizz extends ClassConstantsCollector {
	private final Map<String, MappedClass> renamemap;
	private String className;

	public ClzzVizz(ConstantPool cp, ClassWriter cw, Map<String, MappedClass> renamemap) {
		super(cw, cp);
		this.renamemap = renamemap;
	}

	/**
	 * Called when a class is visited. This is the method called first
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		MappedClass mc = renamemap.get(name);
		className = name;
		if (mc == null) {
			super.visit(version, access, name, signature, superName, interfaces);
			return;
		}
		name = mc.getRenamed();
		if (mc.getParent() != null) {
			superName = mc.getParent().getRenamed();
		}
		signature = StringUtil.fixDesc(signature, renamemap.values());
		for (String intef : interfaces) {
			MappedClass ic = renamemap.get(intef);
			if (ic != null) {
				intef = StringUtil.replace(intef, intef, ic.getRenamed());
			}
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}

	/**
	 * Invoked when a class level annotation is encountered
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return super.visitAnnotation(desc, visible);
	}

	/**
	 * When a class attribute is encountered
	 */
	@Override
	public void visitAttribute(Attribute attr) {
		super.visitAttribute(attr);
	}

	/**
	 * Invoked only when the class being visited is an inner class
	 */
	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		super.visitOuterClass(owner, name, desc);
	}

	/**
	 * When an inner class is encountered
	 */
	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		super.visitInnerClass(name, outerName, innerName, access);
	}

	/**
	 * When a field is encountered
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		MappedClass mc = renamemap.get(className);
		if (mc == null) {
			return new FldVizz(cp, super.visitField(access, name, desc, signature, value), renamemap);
		}
		MappedField mf = mc.getFields().get(name);
		if (mf != null) {
			name = mf.getRenamed();
		}
		desc = StringUtil.fixDesc(desc, renamemap.values());
		signature = StringUtil.fixDesc(signature, renamemap.values());
		return new FldVizz(cp, super.visitField(access, name, desc, signature, value), renamemap);
	}

	/**
	 * When a method is encountered
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MappedClass mc = renamemap.get(className);
		if (mc == null) {
			return new MthVizz(name, cp, renamemap, super.visitMethod(access, name, desc, signature, exceptions));
		}
		MappedMethod mm = mc.getMethods().get(name);
		if (mm != null) {
			name = mm.getRenamed();
		}
		desc = StringUtil.fixDesc(desc, renamemap.values());
		signature = StringUtil.fixDesc(signature, renamemap.values());
		return new MthVizz(name, cp, renamemap, super.visitMethod(access, name, desc, signature, exceptions));
	}

	/**
	 * When the optional source is encountered
	 */
	@Override
	public void visitSource(String source, String debug) {
		super.visitSource(source, debug);
	}

	@Override
	public void visitEnd() {
		Map<Constant, Constant> le = new HashMap<Constant, Constant>();
		le.putAll(cp);
		for (Constant constant : le.keySet()) {
			if (constant.type == 'C') {
				String strVal1 = StringUtil.fixDesc(constant.strVal1, renamemap.values());
				constant.set(constant.type, strVal1, null, null);
			} else if (constant.type == 'S' || constant.type == 's') {
				String strVal1 = constant.strVal1;
				if (strVal1.contains("/")) {
					strVal1 = StringUtil.fixDesc(constant.strVal1, renamemap.values());
				} else {
					String s1 = className + " : " + strVal1;
					strVal1 = StringUtil.fixMethodRefs(strVal1, renamemap.values());
					String s2 = className + " : " + strVal1;
					if (!s1.equals(s2)) {
						constant.set(constant.type, strVal1, null, null);
					}
				}

			} else if (constant.type == 'M' || constant.type == 'G') {
				String strVal1 = StringUtil.fixDesc(constant.strVal1, renamemap.values());
				String objVal3 = StringUtil.fixDesc(constant.objVal3.toString(), renamemap.values());
				constant.set(constant.type, strVal1, constant.strVal2, objVal3);
			} else if (constant.type == 'T') {
				String strVal1 = StringUtil.fixDesc(constant.strVal1, renamemap.values());
				String strVal2 = StringUtil.fixDesc(constant.strVal2, renamemap.values());
				constant.set(constant.type, strVal1, strVal2, null);
			}
			//cp.remove(constant);
			//cp.put(constant, constant);
		}
	}
}
