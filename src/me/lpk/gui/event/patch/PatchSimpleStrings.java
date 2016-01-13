package me.lpk.gui.event.patch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.asm.stringrep.SimpleStringClassVisitor;
import me.lpk.gui.Main;
import me.lpk.gui.stages.StageStringPatch;
import me.lpk.gui.tabs.PatchingTab;
import me.lpk.mapping.MappingGen;
import me.lpk.util.ASMUtil;
import me.lpk.util.JarUtil;

public class PatchSimpleStrings implements EventHandler<ActionEvent> {
	private final StageStringPatch tab;

	public PatchSimpleStrings(StageStringPatch stageStringPatch) {
		this.tab = stageStringPatch;
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
				ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
				cn.accept(new SimpleStringClassVisitor(cw, cn, tab.getOClass(), tab.getOMethod()));
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
