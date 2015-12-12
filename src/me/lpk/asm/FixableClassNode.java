package me.lpk.asm;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A slightly modified ClassNode that removed the duplicate entries created by
 * the remapping process... An ugly hack but it works for now.
 * 
 * Suggestion by Bibl: 'I havent really used asm's remapper. instead I made my
 * own which iterates through all of the classes/fields/methods and then renames
 * stuff that way.'
 */
public class FixableClassNode extends ClassNode {
	public FixableClassNode() {
		super(Opcodes.ASM5);
	}

	@SuppressWarnings("unchecked")
	public void fix() {
		Set<Object> ifaces = new HashSet<>(interfaces);
		interfaces.clear();
		interfaces.addAll(ifaces);
		int size = this.methods.size();
		Set<Object> os = new LinkedHashSet<>();
		for (int i = 0; i < size; i++) {
			MethodNode mn = (MethodNode) this.methods.get(i);
			if (i >= size / 2) {
				os.add(mn);
			}
		}
		methods.clear();
		methods.addAll(os);
		os.clear();
		size = this.fields.size();
		for (int i = 0; i < size; i++) {
			FieldNode fn = (FieldNode) this.fields.get(i);
			if (i >= size / 2) {
				os.add(fn);
			}
		}
		fields.clear();
		fields.addAll(os);
		os.clear();
		size = innerClasses.size();
		for (int i = 0; i < size; i++) {
			InnerClassNode icn = (InnerClassNode) innerClasses.get(i);
			if (i >= size / 2) {
				os.add(icn);
			}
		}
		innerClasses.clear();
		innerClasses.addAll(os);
		os.clear();
	}
}
