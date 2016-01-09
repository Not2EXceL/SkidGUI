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
 * should be fixed for release.
 */
public class MappingGen {
	public static final int NONE = -1, ORDERED_DICTIONARY = 0, RAND_DICTIONARY = 1, SIMPLE = 2, FUCKED = 3, FUK = 166;
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
		} else {
			System.out.println(rename.get(rename.keySet().toArray()[14]).getRenamed());
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
		if (hasParents) {
			boolean parentRenamed = rename.containsKey(cn.superName);
			ClassNode parentNode = nodes.get(cn.superName);
			if (parentNode != null && !parentRenamed) {
				map(parentNode);
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
		for (MethodNode methodNode : classMap.getNode().methods) {
			MappedMethod mappedMethod = getParentMethod(classMap, methodNode);

			if (mappedMethod == null) {
				// If the name is <init> or <clinit>
				if (methodNode.name.contains("<")) {
					mappedMethod = new MappedMethod(methodNode.name, methodNode.name);
				}
				if (mappedMethod == null && AccessHelper.isEnum(methodNode.access)) {
					if (methodNode.name.equals("values") || methodNode.name.equals("getName")|| methodNode.name.equals("ordinal")) {
						mappedMethod = new MappedMethod(methodNode.name, methodNode.name);
					}
				}
				try {
					// THERE HAS TO BE A BETTER WAY OF CHECKING IF THE METHOD
					// BELONGS TO A PARENT CLASS OR INTERFACE
					// THIS IS FUCKING UGLY HOLY SHIT

					Class superClass = Class.forName(methodNode.owner.superName.replace("/", "."));
					Class clazz = Class.forName(methodNode.owner.name.replace("/", "."));
					Class[] interfaces = clazz.getInterfaces();
					
					// Is the class an enum? If so if it is an enum-specific method keep the naming and continue;
					
					// loop through superclasses and see if the method belongs
					// to a parent
					boolean exit = mappedMethod != null;
					while (!exit) {
						for (Method m : superClass.getMethods()) {
							if (m.getName().equals(methodNode.name)) {
								mappedMethod = new MappedMethod(methodNode.name, methodNode.name);
								exit = true;
							}
						}
						superClass = superClass.getSuperclass();
						if (superClass == null || superClass.getClass() == null || superClass.getClass().getName().contains("java.lang.Object")) {
							exit = true;
						}
					}
					// Check the interfaces of the class if it is still null.
					if (mappedMethod == null) {
						
						for (Class interfaze : interfaces) {
							String mapNameOfInterface = interfaze.getName().replace(".", "/");
							boolean haveMapping = rename.containsKey(mapNameOfInterface);
							if (haveMapping) {
								MappedClass mappedInterface = rename.get(mapNameOfInterface);
								if (mappedInterface.getMethods().containsKey(methodNode.name)) {
									mappedMethod = mappedInterface.getMethods().get(methodNode.name);
								}
							} else {
								for (Method m : interfaze.getMethods()) {
									if (m.getName().equals(methodNode.name)) {
										mappedMethod = new MappedMethod(methodNode.name, methodNode.name);
									}
								}
							}
						}
					}
				} catch (ClassNotFoundException | SecurityException | NullPointerException e) {
					//This feels so wrong.... 
					// e.printStackTrace();
				}
				//If the method is STILL null this means it must be totally new. Obfuscate it.
				if (mappedMethod == null) {
					mappedMethod = new MappedMethod(methodNode.name, getMethodName(methodNode, mode));
				}
			}
			//If the method is the main one, notify ourselves it is indeed main.
			if (isMain(methodNode)) {
				mappedMethod = new MappedMethod(methodNode.name, methodNode.name);
				setMain(classMap.getRenamed());
			}
			//Add the method to the mapped class.
			classMap.getMethods().put(methodNode.name, mappedMethod);
			methodIndex++;
		}
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
	 * @param classMap
	 *            Initial node
	 * @param methodNode
	 *            Initial method
	 * @return
	 */
	private static MappedMethod getParentMethod(MappedClass classMap, MethodNode methodNode) {
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
