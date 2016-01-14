package me.lpk.asm.zkm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.asm.MethodTransformer;
import me.lpk.util.OpUtil;

public class ZKMMethodTransformer extends MethodTransformer {
	private final Map<Integer, String> strings = new HashMap<Integer, String>();
	private final Map<Integer, Integer> modifiers = new HashMap<Integer, Integer>();
	private String zkmArrayName;
	private boolean multiZKM = false;

	public ZKMMethodTransformer(ClassNode node) {
		super(node);
		// Set up data within the <clinit> method
		for (MethodNode mnode : node.methods) {
			if (mnode.name.startsWith("<c")) {
				extractStatic(mnode);
				cleanStatic(mnode);
			}
		}
	}

	@Override
	public void transform(MethodNode method) {
		if (!method.name.startsWith("<c")) {
			replaceValues(method);
		}
	}

	/**
	 * Update values of the ZKM String[] with the original strings in a given
	 * method.
	 * 
	 * @param method
	 */
	private void replaceValues(MethodNode method) {
		for (AbstractInsnNode ain : method.instructions.toArray()) {
			// If there are multiple values in the ZKM encrypted field are
			// detected (multiZKM) and the opcode is loading from an array...
			if (multiZKM && ain.getOpcode() == Opcodes.AALOAD) {
				AbstractInsnNode iin = ain.getPrevious();
				int prevOp = iin.getOpcode();
				// If the opcode of the index is 0-5 or a higher integer
				// number...
				if ((prevOp >= Opcodes.ICONST_0 && prevOp <= Opcodes.ICONST_5) || prevOp == Opcodes.BIPUSH)
					// If the node 2 before the current node is a field
					// reference...
					if (ain.getPrevious().getPrevious() instanceof FieldInsnNode) {
						FieldInsnNode fin = (FieldInsnNode) ain.getPrevious().getPrevious();
						// If the field name matches and the desc is a string
						// array...
						if (fin.name.equals(zkmArrayName) && fin.desc.equals("[Ljava/lang/String;")) {
							// If the value has already been decrypted, swap out
							// the value.
							String value = strings.get(OpUtil.getIntValue(iin));
							if (value != null && !value.equals("null")) {
								method.instructions.set(fin, new LdcInsnNode(value));
								method.instructions.remove(iin);
								method.instructions.remove(ain);
							}
						}
					}
			} else if (!multiZKM) {
				// If it's a single string in a static field, check for
				// GetStatic
				if (ain.getOpcode() == Opcodes.GETSTATIC) {
					FieldInsnNode fin = (FieldInsnNode) ain;
					// Does the name match? Is the field desc a string? We got a
					// match!
					if (fin.name == zkmArrayName && fin.desc.equals("Ljava/lang/String;")) {
						method.instructions.set(fin, new LdcInsnNode(strings.get(0)));
					}
				}
			}
		}
	}

	/**
	 * Extracts the strings from the static block and deobfuscates them.
	 * 
	 * @param method
	 */
	private void extractStatic(MethodNode method) {
		for (AbstractInsnNode ain : method.instructions.toArray()) {
			// Setup common to ZKM array and single string
			if (ain.getOpcode() == Opcodes.PUTSTATIC) {
				// Getting the field name for other methods to reference.
				if (ain instanceof FieldInsnNode) {
					FieldInsnNode fin = (FieldInsnNode) ain;

					// If the previous opcode is array storing, it is a multiZKM
					// string setup.
					if (ain.getPrevious().getOpcode() == Opcodes.AASTORE && ain.getNext().getOpcode() == Opcodes.GOTO) {
						zkmArrayName = fin.name;
						multiZKM = true;
					} else {
						// Ok so it's not a multi. Let's see if the value setter
						// is in between two GOTO's.
						int prev = ain.getPrevious().getOpcode();
						if (prev == Opcodes.F_NEW) {
							prev = ain.getPrevious().getPrevious().getOpcode();
						}
						if (prev == Opcodes.GOTO && ain.getNext().getOpcode() == Opcodes.GOTO) {
							zkmArrayName = fin.name;
							multiZKM = false;
						}
					}

				}
			}
		}
		// The rest of this method was written by Quux(qMatt) in Kotlin. I just
		// converted it myself to normal Java.
		List<AbstractInsnNode> lastInsns = new ArrayList<AbstractInsnNode>();
		boolean possibleMatch = false, hasModifiers = false, skip = false;
		int ind = 0;
		// Iterate through the method instructions.

		for (AbstractInsnNode ain : method.instructions.toArray()) {
			// Grab all LDC's that are not the first (The first is the 'key').
			// So skip that one.
			if (ain instanceof LdcInsnNode) {
				Object cst = ((LdcInsnNode) ain).cst;
				if (cst instanceof String) {
					if (!skip) {
						strings.put(ind, (String) cst);
						ind++;
					}
					skip = false;
				}
			} else {
				// Get the modifiers if a match is detected.
				int o = ain.getOpcode();
				if (possibleMatch) {
					if (o != Opcodes.F_NEW && o != Opcodes.TABLESWITCH) {
						lastInsns.add(ain);
					}
					// Match was a mistake. Continue on.
					if (!((o >= Opcodes.ICONST_0 && o <= Opcodes.ICONST_5) || o == Opcodes.BIPUSH || o == Opcodes.GOTO || o == Opcodes.F_NEW || o == Opcodes.TABLESWITCH)) {
						possibleMatch = false;
						lastInsns.clear();
					}
					// Match is valid, populate the modifiers.
					if (lastInsns.size() > 8) {
						hasModifiers = true;
						for (int i = 0; i < 5; i++) {
							int v = OpUtil.getIntValue(lastInsns.get(i * 2));
							modifiers.put(i, v);
						}
						possibleMatch = false;
						lastInsns.clear();
					}
				}
				if (o == Opcodes.TABLESWITCH) {
					possibleMatch = true;
				}
			}
		}
		// For each index in the strings map, decrypt them based on their index
		for (int in : strings.keySet()) {
			strings.replace(in, (decrypt(strings.get(in))));
		}
	}

	/**
	 * Finds the begining of the ZKM blurb, the end, then removed everything in
	 * between!
	 * 
	 * @param method
	 */
	private void cleanStatic(MethodNode method) {

	}

	/**
	 * Decrypts a string based on their index in the array (or alone if only one
	 * string) and the existing modifiers.
	 * 
	 * @param input
	 *            Obfuscated string
	 * @return Deobfuscated string
	 */

	private String decrypt(String input) {
		String decrypted = "";
		int i = 0;
		for (char in : input.toCharArray()) {
			int charInt = ((in) ^ modifiers.get(i % 5));
			decrypted += (char) charInt;
			i++;
		}
		return decrypted;
	}

}