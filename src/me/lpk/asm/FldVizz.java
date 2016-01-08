package me.lpk.asm;

import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.optimizer.ConstantPool;
import org.objectweb.asm.optimizer.FieldConstantsCollector;

import me.lpk.mapping.objects.MappedClass;
@SuppressWarnings("unused")
public class FldVizz extends FieldConstantsCollector {
	private final Map<String, MappedClass> renamemap;

	public FldVizz(ConstantPool cp,FieldVisitor fv,  Map<String, MappedClass> renamemap) {
		super(fv, cp);
		this.renamemap = renamemap;

	}

	@Override
	public void visitAttribute(Attribute attr) {
		super.visitAttribute(attr);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
	}
}
