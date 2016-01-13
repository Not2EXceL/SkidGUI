package me.lpk.util;

import java.util.Collection;
import java.util.Map;

import me.lpk.mapping.objects.MappedClass;
import me.lpk.mapping.objects.MappedField;
import me.lpk.mapping.objects.MappedMethod;

public class StringUtil {
	public static String fixDesc(String description, Collection<MappedClass> values) {
		if (description != null) {
			try {
				for (MappedClass mappedClass : values) {
					if (description.contains(mappedClass.getOriginal())) {
						description = replace(description, mappedClass.getOriginal(), mappedClass.getRenamed());
					}
				}
			} catch (Exception e) {
				System.out.println("Fix[Desc] failed: " + description + " - " + e.getMessage());
			}
		}
		return description;
	}

	public static String fixField(String fieldName, MappedClass mapped) {
		if (fieldName != null && mapped != null) {
			try {
				MappedField mm = mapped.getFields().get(fieldName);
				if (mm != null) {
					fieldName = mm.getRenamed();
				}
			} catch (Exception e) {
				System.out.println("Fix[Mthd] failed: " + fieldName + " - " + e.getMessage());
			}
		}
		return fieldName;
	}

	public static String fixMethod(String methodName, MappedClass mapped) {
		if (methodName != null && mapped != null) {
			try {
				MappedMethod mm = mapped.getMethods().get(methodName);
				if (mm != null) {
					methodName = mm.getRenamed();
				}
			} catch (Exception e) {
				System.out.println("Fix[Mthd] failed: " + methodName + " - " + e.getMessage());
			}
		}
		return methodName;
	}

	/**
	 * Replaces strings with old references with ones with updated references.
	 * 
	 * @param orig
	 * @param oldStr
	 * @param newStr
	 * @return
	 */
	public static String replace(String orig, String oldStr, String newStr) {
		StringBuffer sb = new StringBuffer(orig);
		while (sb.toString().contains(oldStr)) {
			if (orig.contains("(") && orig.contains(";")) {
				int start = sb.indexOf("L" + oldStr) + 1;
				int end = sb.indexOf(oldStr + ";") + oldStr.length();
				if (start > -1 && end <= orig.length()) {
					sb.replace(start, end, newStr);
				} else {
					System.err.println("REPLACE FAIL: (" + oldStr + ") - " + orig);
					break;
				}
			} else {
				if (sb.toString().equals(oldStr)) {
					sb.replace(0, sb.length(), newStr);
				} else {
					// (me/lpk/mapping/objects/MappedClass) - Example
					// TODO: Make a way to handle this in case of class name
					// conflict
					sb.replace(sb.indexOf(oldStr), sb.indexOf(oldStr) + oldStr.length(), newStr);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Gets a MappedClass in renamemap from a class's description
	 * 
	 * @param renamemap
	 * @param desc
	 * @return
	 */
	public static MappedClass getMappedFromDesc(Map<String, MappedClass> renamemap, String desc) {
		if (desc.length() <= 3) {
			return null;
		}
		int beginIndex = desc.indexOf("L");
		int endIndex = desc.indexOf(";");
		if (beginIndex == -1 || endIndex == -1) {
			return null;
		}
		String owner = desc.substring(beginIndex + 1, endIndex);
		return renamemap.get(owner);
	}

	/**
	 * Updates a description's references with every MappedClass given.
	 * 
	 * @param description
	 * @param values
	 * @return
	 */
	public static String fixMethodRefs(String description, Collection<MappedClass> values) {
		if (description != null) {
			try {
				for (MappedClass mappedClass : values) {
					for (MappedMethod mappedMethod : mappedClass.getMethods().values()) {
						if (description.equals(mappedMethod.getOriginal())) {
							description = replace(description, mappedMethod.getOriginal(), mappedMethod.getRenamed());
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Fix[Desc] failed: " + description + " - " + e.getMessage());
			}
		}
		return description;
	}

}
