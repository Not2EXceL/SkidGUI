package me.lpk.event.gui;

import java.io.File;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.asm.FixableClassNode;
import me.lpk.asm.MapperImpl;
import me.lpk.gui.Main;
import me.lpk.gui.tabs.MapTab;
import me.lpk.mapping.MappingGen;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.util.ASMUtil;
import me.lpk.util.JarUtil;

public class SaveJar implements  EventHandler<ActionEvent> {
	private final MapTab tab;

	public SaveJar(MapTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent event) {
		try {
			File jar = Main.getTargetJar();
			MappingGen.setLast(jar);
			Map<String, FixableClassNode> nodes = tab.getNodes();
			Map<String, MappedClass> renamed = tab.getRemap();
			Remapper mapper = new MapperImpl(renamed);
			for (FixableClassNode cn : nodes.values()) {
				ClassReader cr = new ClassReader(ASMUtil.getNodeBytes(cn));
				ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
				RemappingClassAdapter rca = new RemappingClassAdapter(cw, mapper);
				cr.accept(rca, ClassReader.EXPAND_FRAMES);
				cr = new ClassReader(cw.toByteArray());
				cr.accept(cn, ClassReader.EXPAND_FRAMES);
				cn.fix();
			}
			JarUtil.saveAsJar(nodes.values(), jar.getName().replace(".jar", "")+ "_Re.jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
