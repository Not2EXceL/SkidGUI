package me.lpk.mapping;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import me.lpk.asm.FixableClassNode;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.mapping.objects.MappedField;
import me.lpk.mapping.objects.MappedMethod;
import me.lpk.util.Characters;

/**
 * TODO: Make less of the functionality static. Was fine for testing but it
 * should be fixed for release.
 */
public class MappingGen {
	public static final int NONE = -1, ORDERED_DICTIONARY = 0, RAND_DICTIONARY = 1, SIMPLE = 2, FUCKED = 3, FUK = 166;
	private static Set<String> used = new HashSet<String>();
	private static Map<String, FixableClassNode> nodes;
	private static Map<String, MappedClass> rename;
	private static File lastUsed;
	private static int classIndex, fieldIndex, methodIndex;
	private static String mainClass;
	private static int mode;

	/**
	 * Remaps a map of <String(Class names), ClassNode>.
	 * 
	 * @param nameMode
	 * @param nodeMap
	 * @return
	 */
	public static Map<String, MappedClass> getRename(int nameMode, Map<String, FixableClassNode> nodeMap) {
		mode = nameMode;
		nodes = nodeMap;
		rename = new HashMap<String, MappedClass>();
		for (FixableClassNode cn : nodes.values()) {
			map(cn);
		}
		mainClass = rename.get(mainClass).getRenamed();
		classIndex = 0;
		fieldIndex = 0;
		methodIndex = 0;
		used.clear();
		return rename;
	}

	/**
	 * Create mapping for a given node. Checks if the given node has parents and
	 * maps those before mapping the node given.
	 * 
	 * @param cn
	 */
	private static void map(FixableClassNode cn) {
		boolean basic = cn.superName.equals("java/lang/Object");
		// If the class has parents
		if (!basic) {
			// If the parents are not renamed get the parent node and map it
			boolean parentRenamed = rename.containsKey(cn.superName);
			FixableClassNode parentNode = nodes.get(cn.superName);
			if (parentNode != null && !parentRenamed) {
				map(parentNode);
			}
		}
		if (!rename.containsKey(cn.name)) {
			createMapping(cn);
		}
	}

	/**
	 * Create mapping for a given node
	 * 
	 * @param cn
	 */
	private static void createMapping(FixableClassNode cn) {
		classIndex++;
		cn.sourceDebug = "Nope";
		cn.sourceFile = "Nope";
		MappedClass co = new MappedClass(cn.name, getClassName(cn, mode), rename.get(cn.superName));
		addFields(cn, co);
		addMethods(cn, co);
		rename.put(cn.name, co);
	}

	/**
	 * Add mapping for methods in the given node
	 * 
	 * @param cn
	 * @param co
	 */
	private static void addMethods(FixableClassNode cn, MappedClass co) {
		for (int i = 0; i < cn.methods.size(); i++) {
			MethodNode mn = (MethodNode) cn.methods.get(i);
			if (shouldIgnore(mn.name)) {
				continue;
			}
			if (mn.name.equals("main")) {
				mainClass = cn.name;
				continue;
			}
			methodIndex++;
			MappedMethod pmo = getParentMethod(cn, mn);
			MappedMethod mo = new MappedMethod(mn.name, pmo != null ? pmo.getRenamed() : getMethodName(mn, mode));
			co.getMethods().put(mn.name, mo);
		}
		methodIndex = 0;
	}

	/**
	 * Add mapping for fields in the given node
	 * 
	 * @param cn
	 * @param co
	 */
	private static void addFields(FixableClassNode cn, MappedClass co) {
		for (int i = 0; i < cn.fields.size(); i++) {
			FieldNode fn = (FieldNode) cn.fields.get(i);
			if (shouldIgnore(fn.name)) {
				continue;
			}
			fieldIndex++;
			MappedField fo = new MappedField(fn.name, getFieldName(fn, mode));
			co.getFields().put(fn.name, fo);
		}
		fieldIndex = 0;
	}

	/**
	 * Creates a new name for a given class
	 * 
	 * @param cn
	 * @param mode
	 * @return
	 */
	private static String getClassName(FixableClassNode cn, int mode) {
		StringBuilder sb = new StringBuilder();
		if (mode == SIMPLE) {
			sb.append("Class" + classIndex);
		} else if (mode == NONE) {
			sb.append(cn.name);
		} else {
			sb.append(getName(mode, classIndex));
		}
		return sb.toString();
	}

	/**
	 * Creates a new name for a given method
	 * 
	 * @param mn
	 * @param mode
	 * @return
	 */
	private static String getMethodName(MethodNode mn, int mode) {
		StringBuilder sb = new StringBuilder();
		if (mode == SIMPLE) {
			sb.append("method" + methodIndex);
		} else if (mode == NONE) {
			sb.append(mn.name);
		} else {
			sb.append(getName(mode, methodIndex));
		}
		return sb.toString();
	}

	/**
	 * Creates a new name for a given field
	 * 
	 * @param fn
	 * @param mode
	 * @return
	 */
	private static String getFieldName(FieldNode fn, int mode) {
		StringBuilder sb = new StringBuilder();
		if (mode == SIMPLE) {
			sb.append("field" + fieldIndex);
		} else if (mode == NONE) {
			sb.append(fn.name);
		} else {
			sb.append(getName(mode, fieldIndex));
		}
		return sb.toString();
	}

	private static String getName(int mode, int optIndex) {
		StringBuilder sb = new StringBuilder();
		if (mode == ORDERED_DICTIONARY) {
			for (int i = 0; i < optIndex; i++) {
				int mod = (i + 1) % Characters.ALPHABET_BOTH.length;
				boolean even = mod == 0;
				boolean last = i == optIndex - 1;
				if (!even && !last) {
					continue;
				}
				sb.append(Characters.ALPHABET_BOTH[mod]);
			}
		} else if (mode == RAND_DICTIONARY) {
			while (sb.length() == 0 || used.contains(sb.toString())) {
				int randIndex = (int) (Math.random() * Characters.ALPHABET_BOTH.length);
				sb.append(Characters.ALPHABET_BOTH[randIndex]);
			}
			used.add(sb.toString());
		} else if (mode == FUCKED) {
			while (sb.length() < FUK || used.contains(sb.toString())) {
				int randIndex = (int) (Math.random() * Characters.UNICODE.length);
				sb.append(Characters.UNICODE[randIndex]);
			}
			used.add(sb.toString());
		}
		return sb.toString();
	}

	/**
	 * Get the super method object of a given overridden method
	 * 
	 * @param cn
	 *            Initial node
	 * @param mn
	 *            Initial method
	 * @return
	 */
	private static MappedMethod getParentMethod(FixableClassNode cn, MethodNode mn) {
		if (rename.containsKey(cn.superName)) {
			MappedClass co = rename.get(cn.superName);
			MappedMethod mo = co.getMethods().get(mn.name);
			while (co != null && mo == null) {
				FixableClassNode zn = nodes.get(co.getOriginal());
				if (zn == null) {
					break;
				}
				co = rename.get(zn.superName);
				if (co == null) {
					break;
				}
				mo = co.getMethods().get(mn.name);
			}
			return mo;
		}
		return null;
	}

	/**
	 * Checks if the method should be skipped for remapping
	 * 
	 * @param name
	 * @return
	 */
	public static boolean shouldIgnore(String name) {
		if (name.contains("<")) {
			return true;
		} else if (name.contains("$")) {
			return true;
		} else if (name.equals("actionPerformed")) {
			return true;
		} else if (name.equals("toString")) {
			return true;
		} else if (name.equals("valueOf")) {
			return true;
		} else if (name.equals("start")) {
			return true;
		}
		return false;
	}

	/**
	 * Update the last mapped jar file
	 * 
	 * @param jarFile
	 */
	public static void setLast(File jarFile) {
		lastUsed = jarFile;
	}

	/**
	 * Get the last used jar file
	 * 
	 * @return
	 */
	public static File getLast() {
		return lastUsed;
	}

	/**
	 * Get the main class file (Found by mapping the jar file)
	 * 
	 * @return
	 */
	public static String getMain() {
		return mainClass;
	}

	public static void setMain(String name) {
		mainClass = name;
	}

}
