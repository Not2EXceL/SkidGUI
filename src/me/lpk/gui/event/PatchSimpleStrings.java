package me.lpk.gui.event;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.asm.modify.SimpleStringClassVisitor;
import me.lpk.gui.Main;
import me.lpk.gui.tabs.PatchingTab;
import me.lpk.mapping.MappingGen;
import me.lpk.util.ASMUtil;
import me.lpk.util.JarUtil;

public class PatchSimpleStrings implements EventHandler<ActionEvent> {
	private final PatchingTab tab;

	public PatchSimpleStrings(PatchingTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent ae) {
		try {
			File jar = Main.getTargetJar();
			Map<String, ClassNode> nodes = JarUtil.loadClasses(jar);
			MappingGen.setLast(jar);
			Map<String, byte[]> out = new HashMap<String, byte[]>();
			System.out.println("Saving " + nodes.size() + " classes... ");
			int workIndex = 1;
			for (ClassNode cn : nodes.values()) {
				ClassReader cr = new ClassReader(ASMUtil.getNodeBytes(cn));
				ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
				ClassVisitor stringFixer = new SimpleStringClassVisitor(cw, cn, tab.getObfuClass(), tab.getObfuMethod());
				cr.accept(stringFixer, ClassReader.EXPAND_FRAMES);
				cr = new ClassReader(cw.toByteArray());
				cw = new ClassWriter(0);
				cr.accept(cw, 0);
				out.put(cn.name, cw.toByteArray());
				//
				String percentStr = "" + ((workIndex + 0.000000001f) / (nodes.size() - 0.00001f)) * 100;
				percentStr = percentStr.substring(0, percentStr.length() > 5 ? 5 : percentStr.length());
				System.out.println("	" + workIndex + "/" + nodes.size() + " [" + percentStr + "%]");
				workIndex++;
			}
			JarUtil.saveAsJar(out, jar.getName().replace(".jar", "") + "_Re.jar", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
