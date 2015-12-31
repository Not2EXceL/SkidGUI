package me.lpk.asm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import me.lpk.mapping.MappingGen;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.mapping.objects.MappedField;
import me.lpk.mapping.objects.MappedMethod;
import me.lpk.util.OpCodes;
import me.lpk.util.StringUtil;

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
		super(OpCodes.ASM5);
	}

	@SuppressWarnings("rawtypes")
	public void rename(Map<String, MappedClass> renamemap) {
		// rename the class
		MappedClass classmapping = renamemap.get(name);
		name = classmapping.getRenamed();
		outerClass = StringUtil.fixDesc(outerClass, renamemap.values());
		signature = StringUtil.fixDesc(signature, renamemap.values());
		superName = StringUtil.fixDesc(superName, renamemap.values());
		sourceDebug = "Nope";
		sourceFile = "Nope";
		// rename fields and update descriptions
		for (int i = 0; i < fields.size(); i++) {
			FieldNode fn = (FieldNode) fields.get(i);
			fn.desc = StringUtil.fixDesc(fn.desc, renamemap.values());
			fn.signature = StringUtil.fixDesc(fn.signature, renamemap.values());
			MappedField mf = classmapping.getFields().get(fn.name);
			if (mf == null) {
				continue;
			}
			String newName = mf.getRenamed();
			fn.name = newName;
		}
		// rename methods and local variables
		for (int i = 0; i < methods.size(); i++) {
			MethodNode mn = (MethodNode) methods.get(i);
			mn.desc = StringUtil.fixDesc(mn.desc, renamemap.values());
			mn.signature = StringUtil.fixDesc(mn.signature, renamemap.values());
			if (mn.name.equals("main")) {
				MappingGen.setMain(name);
			}
			MappedMethod mm = classmapping.getMethods().get(mn.name);
			if (mm != null) {
				mn.name = mm.getRenamed();
			}
			List localVars = mn.localVariables;
			if (localVars != null) {
				for (Object o : localVars) {
					LocalVariableNode lvn = (LocalVariableNode) o;
					// TODO: Local variable renaming
					lvn.desc = StringUtil.fixDesc(lvn.desc, renamemap.values());
					lvn.signature = StringUtil.fixDesc(lvn.signature, renamemap.values());
				}
			}
			InsnList instructionList = mn.instructions;
			if (instructionList != null) {
				Iterator instructIter = instructionList.iterator();
				while (instructIter.hasNext()) {
					Object o = instructIter.next();
					if (o instanceof AnnotationNode) {
						AnnotationNode an = ((AnnotationNode) o);
						an.desc = StringUtil.fixDesc(an.desc, renamemap.values());
					} else if (o instanceof FieldInsnNode) {
						FieldInsnNode fin = ((FieldInsnNode) o);
						fin.desc = StringUtil.fixDesc(fin.desc, renamemap.values());
						fin.name = StringUtil.fixField(fin.name, renamemap.get(fin.owner));
						fin.owner = StringUtil.fixDesc(fin.owner, renamemap.values());
					} else if (o instanceof InvokeDynamicInsnNode) {
						// I don't think this is ever called
						// Multiple test cases and this is never called
						InvokeDynamicInsnNode idn = ((InvokeDynamicInsnNode) o);
						idn.desc = StringUtil.fixDesc(idn.desc, renamemap.values());
						idn.desc = StringUtil.fixMethod(idn.name, renamemap.get(idn.bsm.getOwner()));
						// TODO: If there's a reason why this isn't called, fix
						// it and do something with this
						int tag = 0;
						String owner = "dank";
						String name = "dank";
						String desc = "dank";
						idn.bsm = new Handle(tag, owner, name, desc);
					} else if (o instanceof MethodInsnNode) {
						MethodInsnNode min = ((MethodInsnNode) o);
						String originalName = min.name;
						min.desc = StringUtil.fixDesc(min.desc, renamemap.values());
						min.name = StringUtil.fixMethod(min.name, renamemap.get(min.owner));
						if (min.name.equals(originalName)) {
							MappedClass cm = classmapping.getParent();
							while (cm != null) {
								String newName = StringUtil.fixMethod(min.name, cm);
								if (!originalName.equals(newName)) {
									originalName = newName;
									break;
								}
								cm = cm.getParent();
							}
							min.name = originalName;
						}
						min.owner = StringUtil.fixDesc(min.owner, renamemap.values());
					} else if (o instanceof MultiANewArrayInsnNode) {
						MultiANewArrayInsnNode manain = ((MultiANewArrayInsnNode) o);
						manain.desc = StringUtil.fixDesc(manain.desc, renamemap.values());
					} else if (o instanceof TypeInsnNode) {
						TypeInsnNode tin = ((TypeInsnNode) o);
						tin.desc = StringUtil.fixDesc(tin.desc, renamemap.values());
					} else if (o instanceof FrameNode) {
						FrameNode fn = ((FrameNode) o);
						List<Object> obList = new ArrayList<Object>();
						for (Object b : fn.local) {
							if (b instanceof String) {
								b = StringUtil.fixDesc(b.toString(), renamemap.values());
							}
							obList.add(b);
						}
						fn.local = obList;
						obList.clear();
						for (Object b : fn.stack) {
							if (b instanceof String) {
								b = StringUtil.fixDesc(b.toString(), renamemap.values());
							}
							obList.add(b);
						}
						fn.stack = obList;
					} else if (o instanceof LdcInsnNode) {
						LdcInsnNode vin = (LdcInsnNode) o;
						if (vin.cst instanceof org.objectweb.asm.Type) {
							org.objectweb.asm.Type type = (Type) vin.cst;
							vin.cst = org.objectweb.asm.Type.getType(StringUtil.fixDesc(vin.cst.toString(), renamemap.values()));
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void removeDuplicates() {
		Set<Object> tmpInterfaces = new HashSet<>(interfaces);
		interfaces.clear();
		interfaces.addAll(tmpInterfaces);
		int size = this.methods.size();
		Set<Object> objectSet = new LinkedHashSet<>();
		for (int i = 0; i < size; i++) {
			MethodNode mn = (MethodNode) this.methods.get(i);
			if (i >= size / 2) {
				objectSet.add(mn);
			}
		}
		methods.clear();
		methods.addAll(objectSet);
		objectSet.clear();
		size = this.fields.size();
		for (int i = 0; i < size; i++) {
			FieldNode fn = (FieldNode) this.fields.get(i);
			if (i >= size / 2) {
				objectSet.add(fn);
			}
		}
		fields.clear();
		fields.addAll(objectSet);
		objectSet.clear();
		size = innerClasses.size();
		for (int i = 0; i < size; i++) {
			InnerClassNode icn = (InnerClassNode) innerClasses.get(i);
			if (i >= size / 2) {
				objectSet.add(icn);
			}
		}
		innerClasses.clear();
		innerClasses.addAll(objectSet);
		objectSet.clear();
	}

	public void patchCustom(String className, String methodName) {
		try {
			for (int x = 0; x < methods.size(); x++) {
				List<Integer> intList = new ArrayList<Integer>();
				MethodNode mn = (MethodNode) methods.get(x);
				if (!mn.name.contains("f")) {
					System.out.println(mn.name);
					continue;
				}
				InsnList newList = new InsnList();
				InsnList instructionList = mn.instructions;
				if (instructionList == null) {
					continue;
				}

				int i = 0;
				@SuppressWarnings("rawtypes")
				Iterator instructIter = instructionList.iterator();
				// System.out.println(name + ":" + mn.name);
				while (instructIter.hasNext()) {
					Object o = instructIter.next();
					if (i >= 52) {
						newList.add((AbstractInsnNode) o);
					}
					if (o instanceof FieldInsnNode) {
						FieldInsnNode fin = ((FieldInsnNode) o);
						String owner = fin.owner;
						if (owner.equals(name)) {
							owner = "this";
						}
						System.out.println(i + " - Field: " + owner + "." + fin.name);
					} else if (o instanceof MethodInsnNode) {
						MethodInsnNode min = ((MethodInsnNode) o);
						String owner = min.owner;
						if (owner.equals(name)) {
							owner = "this";
						}
						System.out.println(i + " - Methd: " + owner + "." + min.name);
					} else if (o instanceof VarInsnNode) {
						VarInsnNode vin = (VarInsnNode) o;
						System.out.println(i + " - Vin: " + OpCodes.opcodes.get(vin.getOpcode()));
					} else if (o instanceof InsnNode) {
						InsnNode isn = ((InsnNode) o);
						System.out.println(i + " - Isn: " + OpCodes.opcodes.get(isn.getOpcode()));
					} else if (o instanceof IntInsnNode) {
						IntInsnNode iin = (IntInsnNode) o;
						System.out.println(i + " - Int: " + OpCodes.opcodes.get(iin.getOpcode()) + " [Value:" + iin.operand + " ]");
						intList.add(iin.operand);
					} else if (o instanceof TypeInsnNode) {
						TypeInsnNode tin = (TypeInsnNode) o;
						System.out.println(i + " - Typ: " + OpCodes.opcodes.get(tin.getOpcode()));
					} else {
						System.out.println(i + " - " + o.getClass().getSimpleName());
					}
					i++;
				}
				mn.instructions.clear();
				mn.instructions = newList;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// When getting the method only accept methods that take one parameter.
		// All I can probably do for now anyways.
	}
}
