package com.example.absoluterandom;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * Мод "Absolute Random": любой сломанный блок вместо своего дропа
 * выдаёт случайный предмет из BuiltInRegistries.ITEM (шанс 100%).
 *
 * Названия классов — Mojang mappings (в новых версиях Minecraft
 * игра поставляется без обфускации, Yarn больше не нужен).
 */
public class AbsoluteRandomMod implements ModInitializer {

    public static final String MOD_ID = "absolute_random";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Предметы, которые нельзя выдавать: технические, ломающие игру, "пустые". */
    private static final Set<Item> BLACKLIST = Set.of(
            Items.AIR,
            Items.COMMAND_BLOCK,
            Items.CHAIN_COMMAND_BLOCK,
            Items.REPEATING_COMMAND_BLOCK,
            Items.COMMAND_BLOCK_MINECART,
            Items.STRUCTURE_BLOCK,
            Items.STRUCTURE_VOID,
            Items.JIGSAW,
            Items.BARRIER,
            Items.LIGHT,
            Items.DEBUG_STICK
    );

    /** Готовый пул предметов. Строится один раз при старте сервера. */
    private static volatile List<Item> itemPool = List.of();

    @Override
    public void onInitialize() {
        // 1) Строим пул, когда реестры и флаги фич уже загружены.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var enabledFeatures = server.overworld().enabledFeatures();

            itemPool = BuiltInRegistries.ITEM.stream()
                    .filter(item -> !BLACKLIST.contains(item))
                    // Отсекаем предметы из отключённых экспериментальных датапаков
                    .filter(item -> item.isEnabled(enabledFeatures))
                    // Страховка: предмет должен давать непустой стек
                    .filter(item -> !new ItemStack(item).isEmpty())
                    .toList();

            LOGGER.info("Absolute Random: в пуле {} предметов", itemPool.size());
        });

        // 2) BEFORE вызывается ДО разрушения блока. Если вернуть false,
        //    ванильное разрушение (и дроп) отменяется — блок ломаем сами.
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            // Работаем только на логическом сервере
            if (!(level instanceof ServerLevel serverLevel)) {
                return true;
            }
            // В творческом режиме дропа нет — не вмешиваемся
            if (player.isCreative()) {
                return true;
            }
            // Пул ещё не готов — ведём себя как ваниль
            List<Item> pool = itemPool;
            if (pool.isEmpty()) {
                return true;
            }

            // Ломаем блок вручную БЕЗ ванильного дропа (false = dropBlock)
            serverLevel.destroyBlock(pos, false, player);

            // Изнашиваем инструмент, как при обычной добыче
            player.getMainHandItem().mineBlock(serverLevel, state, pos, player);

            // Случайный предмет (random мира — потокобезопасно для сервера)
            Item randomItem = pool.get(serverLevel.getRandom().nextInt(pool.size()));
            ItemStack stack = new ItemStack(randomItem, 1);

            // Спавним ItemEntity в центре сломанного блока
            ItemEntity drop = new ItemEntity(
                    serverLevel,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    stack
            );
            drop.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(drop);

            // false => отменяем ванильную обработку (иначе дроп задвоится)
            return false;
        });
    }
}
