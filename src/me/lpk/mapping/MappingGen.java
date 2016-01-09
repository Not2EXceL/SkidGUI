package me.lpk.mapping;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

import me.lpk.mapping.objects.MappedClass;
import me.lpk.mapping.objects.MappedField;
import me.lpk.mapping.objects.MappedMethod;
import me.lpk.util.AccessHelper;
import me.lpk.util.Characters;

/**
 * TODO: Make less of the functionality static. Was fine for testing but it
 * should be fixed for release. Get ready for a sort of future plugin system.
 * 
 * TODO: Remove ALL usage of reflection.
 * 
 * TODO: Make mapping perfect (Is this method belong to a class not in this jar
 * file? Then don't rename it)
 * 
 * TODO: Classes that refer to methods in their parent aren't updated in the
 * class visitor. Is that the visitor's fault or Mapping? May be the visitor
 * saying "Owner is X (Which extends Y). Method is in Y, so X should have it but
 * I wrote code saying only to look in X. So it ignores method inheritence.
 */
public class MappingGen {
	public static final int NONE = -1, ORDERED_DICTIONARY = 0, RAND_DICTIONARY = 1, SIMPLE = 2, UNICODE_NIGHTMARE = 3, UNICODE_MAX_LENGTH = 166;
	private static Set<String> used = new HashSet<String>();
	private static Map<String, ClassNode> nodes;
	private static Map<String, MappedClass> rename;
	private static File lastUsed;
	private static int classIndex, fieldIndex, methodIndex;
	private static String mainClass;
	private static int mode;

	/**
	 * Remaps a map of <String(Class names), ClassNode>.
	 * 
	 * @param nameMode
	 * @param nodes
	 * @return
	 */
	public static Map<String, MappedClass> getRename(int nameMode, Map<String, ClassNode> nodess) {
		mode = nameMode;
		nodes = nodess;
		classIndex = 0;
		fieldIndex = 0;
		methodIndex = 0;
		used.clear();
		rename = new HashMap<String, MappedClass>();
		for (ClassNode cn : nodes.values()) {
			map(cn);
		}
		for (String className : rename.keySet()) {
			MappedClass classMap = rename.get(className);
			// MappedClass has no parent.
			if (classMap.getParent() == null) {
				// Find its parent.
				MappedClass parent = rename.get(classMap.getNode().superName);
				// If found, set it's parent. Have the parent set it as its
				// child.
				if (parent != null) {
					classMap.setParent(parent);
					parent.addChild(classMap);
				}
			} else { // MappedClass has parent.
				// If the parent does not have it as a child, add it.
				if (!classMap.getParent().getChildren().contains(classMap)) {
					classMap.getParent().addChild(classMap);
				}
			}
		}
		boolean log = false;
		if (log) {
			Genson g = new GensonBuilder().useIndentation(false).setSkipNull(true).exclude(ClassNode.class).exclude("parent", MappedClass.class).exclude("children", MappedClass.class).exclude("fields", MappedClass.class).create();
			System.out.println(g.serialize(rename).trim());
		}
		return rename;
	}

	/**
	 * Create mapping for a given node. Checks if the given node has parents and
	 * maps those before mapping the node given.
	 * 
	 * @param cn
	 */
	private static void map(ClassNode cn) {
		boolean hasParents = !cn.superName.equals("java/lang/Object");
		boolean hasInterfaces = cn.interfaces.size() > 0;
		if (hasParents) {
			boolean parentRenamed = rename.containsKey(cn.superName);
			ClassNode parentNode = nodes.get(cn.superName);
			if (parentNode != null && !parentRenamed) {
				map(parentNode);
			}
		}
		if (hasInterfaces) {
			for (String interfaze : cn.interfaces) {
				boolean interfaceRenamed = rename.containsKey(interfaze);
				ClassNode interfaceNode = nodes.get(interfaze);
				if (interfaceNode != null && !interfaceRenamed) {
					map(interfaceNode);
				}
			}
		}
		boolean isRenamed = rename.containsKey(cn.name);
		if (!isRenamed) {
			mapClass(cn);
		}
	}

	private static void mapClass(ClassNode cn) {
		classIndex++;
		MappedClass classMap = new MappedClass(cn, getClassName(cn, mode), rename.get(cn.superName));
		addFields(classMap);
		addMethods(classMap);
		rename.put(cn.name, classMap);
	}

	private static void addFields(MappedClass classMap) {
		for (FieldNode fieldNode : classMap.getNode().fields) {
			MappedField mappedField = new MappedField(fieldNode.name, getFieldName(fieldNode, mode));
			classMap.getFields().put(fieldNode.name, mappedField);
			fieldIndex++;
		}
	}

	private static void addMethods(MappedClass classMap) {
		Set<String> syntheticMethods = new HashSet<String>();
		for (MethodNode methodNode : classMap.getNode().methods) {
			if (AccessHelper.isSynthetic(methodNode.access)) {
				syntheticMethods.add(methodNode.name);
			}
		}
		for (MethodNode methodNode : classMap.getNode().methods) {
			MappedMethod mappedMethod = null;
			// If the method is the main one, make sure it's output name is the
			// same as the intial name. Mark it as the main method as well.
			if (isMain(methodNode)) {
				mappedMethod = new MappedMethod(methodNode.name, methodNode.name);
				setMain(classMap.getRenamed());
			} else if (methodNode.name.contains("<")) {
				// If the name is <init> or <clinit>
				mappedMethod = new MappedMethod(methodNode.name, methodNode.name);
			} else if (syntheticMethods.contains(methodNode.name)) {
				// The method shares a synthetic method's name. It most likely
				// should not be renamed.
				mappedMethod = new MappedMethod(methodNode.name, methodNode.name);
			} else {
				// If the method is not the main method and not <init>/<clinit>,
				// attempt to find it in a parent class.
				mappedMethod = getParentMethod(classMap, methodNode);
			}
			// If the method belongs to an enum and is an inbuilt method
			// belonging to the Enum class.
			if (mappedMethod == null && AccessHelper.isEnum(classMap.getNode().access)) {
				if (methodNode.name.equals("values") || methodNode.name.equals("getName") || methodNode.name.equals("ordinal")) {
					mappedMethod = new MappedMethod(methodNode.name, methodNode.name);
				}
			}
			// If the method is still null, attempt to find it in
			// the interfaces.
			if (mappedMethod == null) {
				mappedMethod = getInterfaceMethod(classMap, methodNode);
			}
			// Use reflection to see if a parent class has the method
			if (mappedMethod == null) {
				mappedMethod = getReflectionParent(classMap, methodNode);
			}
			// If the method is STILL null this means it must be totally
			// new. Obfuscate it.
			if (mappedMethod == null) {
				mappedMethod = new MappedMethod(methodNode.name, getMethodName(methodNode, mode));
			}
			// Add the method to the mapped class.
			classMap.getMethods().put(methodNode.name, mappedMethod);
			methodIndex++;
		}
	}

	/**
	 * Attempt to find the given method in a parent class, given the inital
	 * class the method belongs do.
	 * 
	 * @param classMap
	 *            Initial class
	 * @param methodNode
	 *            Initial method
	 * @return
	 */
	private static MappedMethod getParentMethod(final MappedClass classMap, final MethodNode methodNode) {
		MappedClass parentMap = classMap.getParent();
		while (parentMap != null) {
			if (parentMap.getMethods().containsKey(methodNode.name)) {
				return parentMap.getMethods().get(methodNode.name);
			}
			parentMap = parentMap.getParent();
		}
		return null;
	}

	/**
	 * Attempt to find the given method in an interface, given the inital class
	 * the method belongs do.
	 * 
	 * @param classMap
	 *            Inital class
	 * @param methodNode
	 *            Initial method
	 * @return
	 */
	private static MappedMethod getInterfaceMethod(final MappedClass classMap, final MethodNode methodNode) {
		MappedClass parentMap = classMap;
		while (parentMap != null) {
			ClassNode node = parentMap.getNode();
			for (String interfaze : node.interfaces) {
				if (rename.containsKey(interfaze)) {
					MappedClass mappedInterface = rename.get(interfaze);
					if (mappedInterface.getMethods().containsKey(methodNode.name)) {
						return mappedInterface.getMethods().get(methodNode.name);
					}
				}
			}
			parentMap = parentMap.getParent();
		}

		return null;
	}

	private static MappedMethod getReflectionParent(final MappedClass classMap, final MethodNode methodNode) {
		try {
			Class<?> clazz = Class.forName(methodNode.owner.name.replace("/", "."));
			if (clazz != null) {
				clazz = clazz.getSuperclass();
			}
			while (clazz != null) {
				String asmName = clazz.getName().replace(".", "/");
				for (Method method : clazz.getMethods()) {
					if (method.getName().equals(methodNode.name)) {
						if (rename.containsKey(asmName)) {
							MappedClass mc = rename.get(asmName);
							if (mc.getMethods().containsKey(methodNode.name)) {
								return mc.getMethods().get(methodNode.name);
							}
						} else {
							return new MappedMethod(methodNode.name, methodNode.name);
						}
					}
				}
				if (clazz.getName().contains("java.lang.Object")) {
					clazz = null;
					break;
				} else {
					clazz = clazz.getSuperclass();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
		} else if (name.equals("handle")) {
			return true;
		} else if (name.equals("values")) {
			return true;
		}
		return false;
	}

	/**
	 * Creates a new name for a given class
	 * 
	 * @param cn
	 * @param mode
	 * @return
	 */
	private static String getClassName(ClassNode cn, int mode) {
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
		} else if (mode == UNICODE_NIGHTMARE) {
			while (sb.length() < UNICODE_MAX_LENGTH || used.contains(sb.toString())) {
				int randIndex = (int) (Math.random() * Characters.UNICODE.length);
				sb.append(Characters.UNICODE[randIndex]);
			}
			used.add(sb.toString());
		}
		return sb.toString();
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

	private static boolean isMain(MethodNode methodNode) {
		if (!methodNode.name.equals("main")) {
			return false;
		}
		if (!methodNode.desc.equals("([Ljava/lang/String;)V")) {
			return false;
		}
		return true;
	}

	public static void setMain(String name) {
		mainClass = name;
	}

}
