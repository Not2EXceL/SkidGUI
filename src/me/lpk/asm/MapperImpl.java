package me.lpk.asm;

import java.util.Map;

import org.objectweb.asm.commons.Remapper;

import me.lpk.mapping.objects.MappedClass;
import me.lpk.mapping.objects.MappedField;
import me.lpk.mapping.objects.MappedMethod;

public class MapperImpl extends Remapper {
	private final Map<String, MappedClass> classes;

	public MapperImpl(Map<String, MappedClass> classes) {
		this.classes = classes;
	}

	@Override
	public String map(String type) {
		if (classes == null) {
			return type;
		}
		if (classes.containsKey(type)) {
			return classes.get(type).getRenamed();
		}
		return type;
	}

	@Override
	public String mapFieldName(String owner, String name, String desc) {
		MappedClass co = classes.get(owner);
		if (co == null) {
			return name;
		}
		MappedField fo = co.getFields().get(name);
		while (fo == null) {
			co = classes.get(co.getOriginal()).getParent();
			if (co == null) {
				return name;
			}
			fo = co.getFields().get(name);
		}
		return fo.getRenamed();
	}

	@Override
	public String mapMethodName(String owner, String name, String desc) {
		MappedClass co = classes.get(owner);
		if (co == null) {
			return name;
		}
		MappedMethod mo = co.getMethods().get(name);
		while (mo == null) {
			co = classes.get(co.getOriginal()).getParent();
			if (co == null) {
				return name;
			}
			mo = co.getMethods().get(name);
		}
		return mo.getRenamed();
	}
}
