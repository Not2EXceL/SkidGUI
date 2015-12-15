package me.lpk.util;

import java.util.Collection;

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

	public static String replace(String orig, String oldStr, String newStr) {
		StringBuffer sb = new StringBuffer(orig);
		sb.replace(sb.indexOf(oldStr), sb.indexOf(oldStr) + oldStr.length(), newStr);
		return sb.toString();
	}

}
