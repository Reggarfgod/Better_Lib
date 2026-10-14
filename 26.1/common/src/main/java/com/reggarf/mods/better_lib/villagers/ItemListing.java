package com.reggarf.mods.better_lib.villagers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;

@FunctionalInterface
public interface ItemListing {
    MerchantOffer getOffer(ServerLevel serverLevel, Entity trader, RandomSource random);
}
