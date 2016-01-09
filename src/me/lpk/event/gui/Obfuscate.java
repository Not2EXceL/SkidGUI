package me.lpk.event.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.optimizer.Constant;
import org.objectweb.asm.optimizer.ConstantPool;
import org.objectweb.asm.optimizer.Shrinker.ConstantComparator;
import org.objectweb.asm.tree.ClassNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.asm.SkidMapper;
import me.lpk.gui.Main;
import me.lpk.gui.tabs.ObfuscationTab;
import me.lpk.mapping.MappingGen;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.util.ASMUtil;
import me.lpk.util.Classpather;
import me.lpk.util.JarUtil;
import me.lpk.util.Timer;

public class Obfuscate implements EventHandler<ActionEvent> {
	private final ObfuscationTab tab;

	public Obfuscate(ObfuscationTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent event) {
		try {
			Timer t = new Timer();
			File jar = Main.getTargetJar();
			
			MappingGen.setLast(jar);
			Map<String, ClassNode> nodes = JarUtil.loadClasses(jar);
			System.out.println("Generating new names for " + nodes.size() + " classes... ");
			Map<String, MappedClass> renamed = MappingGen.getRename(tab.getObfuscation(), nodes);
			Map<String, byte[]> out = new HashMap<String, byte[]>();
			System.out.println("Renaming " + renamed.size() + " classes... ");
			int workIndex = 1;
			SkidMapper mapper = new SkidMapper(renamed);
			for (ClassNode cn : nodes.values()) {
				ClassReader cr = new ClassReader(ASMUtil.getNodeBytes(cn));
				ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
				ClassVisitor remapper = new RemappingClassAdapter(cw, mapper);
				cr.accept(remapper, ClassReader.EXPAND_FRAMES);
				cr = new ClassReader(cw.toByteArray());
				cw = new ClassWriter(0);
				cr.accept(cw, ClassReader.SKIP_DEBUG);			
				out.put(renamed.get(cn.name).getRenamed(), cw.toByteArray());
				//
				String percentStr = "" + ((workIndex + 0.000000001f) / (renamed.size() - 0.00001f)) * 100;
				percentStr = percentStr.substring(0, percentStr.length() > 5 ? 5 : percentStr.length());
				System.out.println("	" + workIndex + "/" + renamed.size() + " [" + percentStr + "%]");
				workIndex++;
			}
			System.out.println("Saving classes...");
			JarUtil.saveAsJar(out, tab.getExportedName(), tab.yes());
			System.out.println("Done! Completion time: " + (t.getTime()) + " Milliseconds");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
