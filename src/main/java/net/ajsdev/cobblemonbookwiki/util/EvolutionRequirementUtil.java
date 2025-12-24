package net.ajsdev.cobblemonbookwiki.util;

import com.cobblemon.mod.common.api.conditional.RegistryLikeCondition;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement;
import com.cobblemon.mod.common.api.spawning.TimeRange;
import com.cobblemon.mod.common.api.spawning.condition.MoonPhase;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.pokemon.requirements.*;
import com.cobblemon.mod.common.registry.StructureIdentifierCondition;
import com.cobblemon.mod.common.registry.StructureTagCondition;
import kotlin.ranges.IntRange;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvolutionRequirementUtil {

    public static String getReadableString(Requirement req, RegistryAccess ra) {

        if (req instanceof AreaRequirement ar) {
            AABB box = ar.getBox();
            Vec3 min = box.getMinPosition();
            Vec3 max = box.getMaxPosition();
            return Component.translatable(
                    "cobblemon_book_wiki.req.area",
                    min.x, min.y, min.z,
                    max.x, max.y, max.z
            ).getString();
        }

        if (req instanceof AttackDefenceRatioRequirement adr) {
            switch (adr.getRatio()) {
                case ATTACK_HIGHER:
                    return Component.translatable("cobblemon_book_wiki.req.attack_higher").getString();
                case DEFENCE_HIGHER:
                    return Component.translatable("cobblemon_book_wiki.req.defence_higher").getString();
                case EQUAL:
                    return Component.translatable("cobblemon_book_wiki.req.attack_equals_defence").getString();
                default:
                    return Component.translatable("cobblemon_book_wiki.req.unknown").getString();
            }
        }

        if (req instanceof BattleCriticalHitsRequirement bchr) {
            return Component.translatable(
                    "cobblemon_book_wiki.req.critical_hits",
                    bchr.getAmount()
            ).getString();
        }

        if (req instanceof BiomeRequirement br) {
            RegistryLikeCondition<Biome> cond = br.getBiomeCondition();
            RegistryLikeCondition<Biome> anti = br.getBiomeAnticondition();
            Registry<Biome> biomeRegistry = ra.registryOrThrow(Registries.BIOME);

            List<String> allowed = new ArrayList<>();
            List<String> denied = new ArrayList<>();

            biomeRegistry.keySet().forEach(rl -> {
                Biome biome = biomeRegistry.get(rl);
                if (cond != null && cond.fits(biome, biomeRegistry)) allowed.add(rl.getPath());
                if (anti != null && anti.fits(biome, biomeRegistry)) denied.add(rl.getPath());
            });

            if (!allowed.isEmpty() && !denied.isEmpty())
                return Component.translatable(
                        "cobblemon_book_wiki.req.biomes.allowed_denied",
                        String.join(", ", allowed),
                        String.join(", ", denied)
                ).getString();

            if (!allowed.isEmpty())
                return Component.translatable(
                        "cobblemon_book_wiki.req.biomes.allowed",
                        String.join(", ", allowed)
                ).getString();

            if (!denied.isEmpty())
                return Component.translatable(
                        "cobblemon_book_wiki.req.biomes.denied",
                        String.join(", ", denied)
                ).getString();

            return Component.translatable("cobblemon_book_wiki.req.biomes.any").getString();
        }

        if (req instanceof BlocksTraveledRequirement btr) {
            return Component.translatable(
                    "cobblemon_book_wiki.req.blocks_travelled",
                    btr.getAmount()
            ).getString();
        }

        if (req instanceof DamageTakenRequirement dtr) {
            return Component.translatable(
                    "cobblemon_book_wiki.req.damage_taken",
                    dtr.getAmount()
            ).getString();
        }

        if (req instanceof DefeatRequirement dr) {
            return Component.translatable(
                    "cobblemon_book_wiki.req.defeat",
                    dr.getAmount(),
                    dr.getTarget().getOriginalString(),
                    dr.getAmount() == 1 ? "" : "s"
            ).getString();
        }

        if (req instanceof FriendshipRequirement fr) {
            return Component.translatable(
                    "cobblemon_book_wiki.req.friendship",
                    fr.getAmount()
            ).getString();
        }

        if (req instanceof HeldItemRequirement hir) {
            Optional<HolderSet<Item>> cond = hir.getItemCondition().items();
            if (cond.isEmpty())
                return Component.translatable("cobblemon_book_wiki.req.held_item.unknown").getString();

            List<String> items = cond.get().stream()
                    .map(h -> h.value().getName(new ItemStack(h.value())).getString())
                    .toList();

            return Component.translatable(
                    "cobblemon_book_wiki.req.held_item",
                    String.join(", ", items)
            ).getString();
        }

        if (req instanceof LevelRequirement lr) {
            if (lr.getMaxLevel() == Integer.MAX_VALUE)
                return Component.translatable(
                        "cobblemon_book_wiki.req.level.min",
                        lr.getMinLevel()
                ).getString();

            return Component.translatable(
                    "cobblemon_book_wiki.req.level.range",
                    lr.getMinLevel(),
                    lr.getMaxLevel()
            ).getString();
        }

        if (req instanceof MoonPhaseRequirement mpr) {
            return Component.translatable(
                    "cobblemon_book_wiki.req.moon_phase",
                    mpr.getMoonPhase().name().toLowerCase().replace('_', ' ')
            ).getString();
        }

        if (req instanceof MoveSetRequirement msr) {
            return Component.translatable(
                    "cobblemon_book_wiki.req.knows_move",
                    StringUtils.capitalize(msr.getMove().getName())
            ).getString();
        }

        if (req instanceof MoveTypeRequirement mtr) {
            ElementalType type = mtr.getType();
            return Component.translatable(
                    "cobblemon_book_wiki.req.move_type",
                    StringUtils.capitalize(type.getName().toLowerCase())
            ).getString();
        }

        if (req instanceof PartyMemberRequirement pmr) {
            return Component.translatable(
                    pmr.getContains()
                            ? "cobblemon_book_wiki.req.party.contains"
                            : "cobblemon_book_wiki.req.party.not_contains",
                    StringUtils.capitalize(pmr.getTarget().getOriginalString())
            ).getString();
        }

        if (req instanceof StructureRequirement sr) {
            List<String> allowed = new ArrayList<>();
            List<String> denied = new ArrayList<>();

            if (sr.getStructureCondition() instanceof StructureTagCondition stc)
                allowed.add(stc.getTag().location().getPath());
            if (sr.getStructureCondition() instanceof StructureIdentifierCondition sic)
                allowed.add(sic.getIdentifier().getPath());

            if (sr.getStructureAnticondition() instanceof StructureTagCondition stc)
                denied.add(stc.getTag().location().getPath());
            if (sr.getStructureAnticondition() instanceof StructureIdentifierCondition sic)
                denied.add(sic.getIdentifier().getPath());

            if (!allowed.isEmpty() && !denied.isEmpty())
                return Component.translatable(
                        "cobblemon_book_wiki.req.structures.allowed_denied",
                        String.join(", ", allowed),
                        String.join(", ", denied)
                ).getString();

            if (!allowed.isEmpty())
                return Component.translatable(
                        "cobblemon_book_wiki.req.structures.allowed",
                        String.join(", ", allowed)
                ).getString();

            if (!denied.isEmpty())
                return Component.translatable(
                        "cobblemon_book_wiki.req.structures.denied",
                        String.join(", ", denied)
                ).getString();

            return Component.translatable("cobblemon_book_wiki.req.structures.any").getString();
        }

        if (req instanceof TimeRangeRequirement tr) {
            TimeRange range = tr.getRange();
            if (range.getRanges().isEmpty())
                return Component.translatable("cobblemon_book_wiki.req.time.any").getString();

            List<String> segments = new ArrayList<>();
            for (IntRange r : range.getRanges()) {
                int sh = (r.getStart() / 1000 + 6) % 24;
                int eh = (r.getEndInclusive() / 1000 + 6) % 24;
                segments.add(String.format("%02d:00–%02d:00", sh, eh));
            }

            return Component.translatable(
                    "cobblemon_book_wiki.req.time.range",
                    String.join(", ", segments)
            ).getString();
        }

        if (req instanceof WeatherRequirement wr) {
            List<String> parts = new ArrayList<>();
            if (wr.isRaining() != null)
                parts.add(wr.isRaining()
                        ? Component.translatable("cobblemon_book_wiki.common.raining").getString()
                        : Component.translatable("cobblemon_book_wiki.common.not_raining").getString());
            if (wr.isThundering() != null)
                parts.add(wr.isThundering()
                        ? Component.translatable("cobblemon_book_wiki.common.thundering").getString()
                        : Component.translatable("cobblemon_book_wiki.common.not_thundering").getString());

            if (parts.isEmpty())
                return Component.translatable("cobblemon_book_wiki.req.weather.any").getString();

            return Component.translatable(
                    "cobblemon_book_wiki.req.weather.must_be",
                    String.join(" y ", parts)
            ).getString();
        }

        if (req instanceof WorldRequirement wr) {
            ResourceLocation id = wr.getIdentifier();
            String world = StringUtils.capitalize(id.getPath().replace('_', ' '));
            return Component.translatable(
                    "cobblemon_book_wiki.req.world",
                    world
            ).getString();
        }

        return Component.translatable("cobblemon_book_wiki.req.unknown").getString();
    }
}
