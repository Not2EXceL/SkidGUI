package me.lpk.event.gui;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.optimizer.Constant;
import org.objectweb.asm.optimizer.ConstantPool;
import org.objectweb.asm.optimizer.Shrinker.ConstantComparator;
import org.objectweb.asm.tree.ClassNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import me.lpk.asm.ClzzVizz;
import me.lpk.gui.Main;
import me.lpk.gui.tabs.ObfuscationTab;
import me.lpk.mapping.MappingGen;
import me.lpk.mapping.objects.MappedClass;
import me.lpk.util.ASMUtil;
import me.lpk.util.JarUtil;

public class Obfuscate implements EventHandler<ActionEvent> {
	private final ObfuscationTab tab;

	public Obfuscate(ObfuscationTab tab) {
		this.tab = tab;
	}

	@Override
	public void handle(ActionEvent event) {
		try {
			File jar = Main.getTargetJar();
			MappingGen.setLast(jar);
			Map<String, ClassNode> nodes = JarUtil.loadClasses(jar);
			Map<String, MappedClass> renamed = MappingGen.getRename(tab.getObfuscation(), nodes);
			Map<String, byte[]> out = new HashMap<String, byte[]>();
			System.out.println("Renaming " + renamed.size() + " classes... ");
			int workIndex = 1;

			for (ClassNode cn : nodes.values()) {
				ConstantPool cp = new ConstantPool();
				ClassReader cr = new ClassReader(ASMUtil.getNodeBytes(cn));
				ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);// 0
				ClzzVizz vizz = new ClzzVizz(cp, cw, renamed);
				cr.accept(vizz, ClassReader.EXPAND_FRAMES);// ClassReader.SKIP_DEBUG
				Set<Constant> constants = new TreeSet<Constant>(new ConstantComparator());
				constants.addAll(cp.values());
				cr = new ClassReader(cw.toByteArray());
				cw = new ClassWriter(0);
				Iterator<Constant> i = constants.iterator();
				while (i.hasNext()) {
					Constant c = i.next();
					c.write(cw);
				}
				cr.accept(cw, ClassReader.SKIP_DEBUG);			
				out.put(renamed.get(cn.name).getRenamed(), cw.toByteArray());
				//
				String percentStr = "" + ((workIndex + 0.000000001f) / (renamed.size() - 0.00001f)) * 100;
				percentStr = percentStr.substring(0, percentStr.length() > 5 ? 5 : percentStr.length());
				System.out.println("	" + workIndex + "/" + renamed.size() + " [" + percentStr + "%]");
				workIndex++;
			}
			JarUtil.saveAsJar(out, tab.getExportedName(), tab.yes());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
