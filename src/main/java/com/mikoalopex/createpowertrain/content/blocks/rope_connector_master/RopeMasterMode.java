package com.mikoalopex.createpowertrain.content.blocks.rope_connector_master;

import net.minecraft.util.StringRepresentable;

import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;

/**
 * Redstone-triggered endpoint action performed by a rope manager.
 */
public enum RopeMasterMode implements StringRepresentable, INamedIconOptions {
	IDLE("idle"),
	FIXED("fixed"),
	FREE("free");

	private final String serializedName;

	RopeMasterMode(String serializedName) {
		this.serializedName = serializedName;
	}

	public RopeMasterMode next() {
		RopeMasterMode[] values = values();
		return values[(ordinal() + 1) % values.length];
	}

	@Override
	public String getSerializedName() {
		return serializedName;
	}

	@Override
	public AllIcons getIcon() {
		return switch (this) {
			case IDLE -> AllIcons.I_PAUSE;
			case FIXED -> AllIcons.I_TARGET;
			case FREE -> AllIcons.I_DICE;
		};
	}

	@Override
	public String getTranslationKey() {
		return "createpowertrain.rope_master.mode." + serializedName;
	}
}
