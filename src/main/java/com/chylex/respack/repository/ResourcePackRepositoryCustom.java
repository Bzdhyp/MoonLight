package com.chylex.respack.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.settings.GameSettings;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ResourcePackRepositoryCustom extends ResourcePackRepository {
	public static Entry createEntryInstance(ResourcePackRepository repository, File file) {
		try {
			if (entryConstructor == null) {
				entryConstructor = Entry.class.getDeclaredConstructor(ResourcePackRepository.class, File.class);
				entryConstructor.setAccessible(true);
			}

			return entryConstructor.newInstance(repository, file);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	private static Constructor<Entry> entryConstructor;

	private List<Entry> repositoryEntriesAll = Lists.newArrayList();
	private final List<Entry> repositoryEntries = Lists.newArrayList();
	private final boolean isReady;

	public ResourcePackRepositoryCustom(File dirResourcepacks, File dirServerResourcepacks, IResourcePack rprDefaultResourcePack, IMetadataSerializer rprMetadataSerializer, GameSettings settings, List<String> enabledPacks) {
		super(dirResourcepacks, dirServerResourcepacks, rprDefaultResourcePack, rprMetadataSerializer, settings);

		isReady = true;
		updateRepositoryEntriesAll();

		for (String pack : enabledPacks) {
			for (Entry entry : repositoryEntriesAll) {
				if (entry.getResourcePackName().equals(pack)) {
					// removed incompatible resource pack removal
					repositoryEntries.add(entry);
				}
			}
		}
	}

	private List<File> getResourcePackFiles(File root) {
		if (root.isDirectory()) {
			List<File> packFiles = Lists.newArrayList();

			for (File file : Objects.requireNonNull(root.listFiles())) {
				if (file.isDirectory() && !new File(file, "pack.mcmeta").isFile()) {
					packFiles.addAll(getResourcePackFiles(file));
				} else {
					packFiles.add(file);
				}
			}

			return packFiles;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public void updateRepositoryEntriesAll() {
		if (!isReady) return;

		List<Entry> list = Lists.newArrayList();

		for (File file : getResourcePackFiles(getDirResourcepacks())) {
			Entry entry = createEntryInstance(this, file);

			if (!repositoryEntriesAll.contains(entry)) {
				try {
					entry.updateResourcePack();
					list.add(entry);
				} catch (Exception e) {
					list.remove(entry);
				}
			} else {
				int index = repositoryEntriesAll.indexOf(entry);

				if (index > -1 && index < repositoryEntriesAll.size()) {
					list.add(repositoryEntriesAll.get(index));
				}
			}
		}

		repositoryEntriesAll.removeAll(list);

		for (Entry entry : repositoryEntriesAll) {
			entry.closeResourcePack();
		}

		repositoryEntriesAll = list;
	}

	@Override
	public List<Entry> getRepositoryEntriesAll() {
		return ImmutableList.copyOf(repositoryEntriesAll);
	}

	@Override
	public List<Entry> getRepositoryEntries() {
		return ImmutableList.copyOf(repositoryEntries);
	}

	@Override
	public void setRepositories(List<Entry> repositories) {
		repositoryEntries.clear();
		repositoryEntries.addAll(repositories);
	}
}
