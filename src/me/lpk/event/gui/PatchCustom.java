package me.lpk.event.gui;

import java.io.File;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.asm.FixableClassNode;
import me.lpk.gui.Main;
import me.lpk.gui.tabs.PatchingTab;
import me.lpk.mapping.MappingGen;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.util.ASMUtil;
import me.lpk.util.JarUtil;

public class PatchCustom implements EventHandler<ActionEvent> {
	@SuppressWarnings("unused")
	private final PatchingTab tab;

	public PatchCustom(PatchingTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent ae) {
		// cc.class method a(int ...)
		File jar = Main.getTargetJar();
		try {
			MappingGen.setLast(jar);
			Map<String, FixableClassNode> nodes = JarUtil.loadClasses(jar);
			for (FixableClassNode cn : nodes.values()) {
				//ClassReader cr = new ClassReader(ASMUtil.getNodeBytes(cn));
				//ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
				cn.patchCustom("cc", "a");
				//cr.accept(cw, ClassReader.EXPAND_FRAMES);
				//cr = new ClassReader(cw.toByteArray());
			}
			JarUtil.saveAsJar(nodes.values(), "StringFixed.jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
