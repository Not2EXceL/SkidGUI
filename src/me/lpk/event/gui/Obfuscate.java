package me.lpk.event.gui;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import jdk.internal.org.objectweb.asm.util.CheckClassAdapter;
import me.lpk.asm.FixableClassNode;
import me.lpk.gui.Main;
import me.lpk.gui.tabs.ObfuscationTab;
import me.lpk.mapping.MappingGen;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.util.ASMUtil;
import me.lpk.util.JarUtil;

public class Obfuscate implements  EventHandler<ActionEvent> {
	private final ObfuscationTab tab;

	public Obfuscate(ObfuscationTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent event) {
		try {
			File jar = Main.getTargetJar();
			MappingGen.setLast(jar);
			Map<String, FixableClassNode> nodes = JarUtil.loadClasses(jar);
			Map<String, MappedClass> renamed = MappingGen.getRename(tab.getObfuscation(), nodes);
			System.out.println("Renaming " + renamed.size() + " classes... ");
			int workIndex = 0;
			for (FixableClassNode cn : nodes.values()) {
				ClassReader cr = new ClassReader(ASMUtil.getNodeBytes(cn));
				ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
				cn.rename(renamed);
				//cn.accept(new CheckClassAdapter(cw));
				cr.accept(cw, ClassReader.EXPAND_FRAMES);
				cr = new ClassReader(cw.toByteArray());
				String percentStr = ""+((workIndex  + 0.000000001f)/(renamed.size() - 0.00001f ))*100;
				percentStr = percentStr.substring(0, percentStr.length() > 5 ? 5 : percentStr.length());
				System.out.println("	" + workIndex + "/" + renamed.size() + " [" + percentStr + "%]");workIndex++;
			}
			JarUtil.saveAsJar(nodes.values(), tab.getExportedName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
