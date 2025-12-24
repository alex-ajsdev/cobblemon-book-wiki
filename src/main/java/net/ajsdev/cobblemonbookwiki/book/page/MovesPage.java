package net.ajsdev.cobblemonbookwiki.book.page;

import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.api.pokemon.moves.Learnset;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.api.types.ElementalTypes;
import com.cobblemon.mod.common.pokemon.FormData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MovesPage {

    public static List<Component> build(FormData formData) {
        Learnset learnset = formData.getMoves();
        Map<Integer, List<MoveTemplate>> levelUpMoves = learnset.getLevelUpMoves();
        List<MoveTemplate> eggMoves = learnset.getEggMoves();
        List<MoveTemplate> tutorMoves = learnset.getTutorMoves();
        List<MoveTemplate> tmMoves = learnset.getTmMoves();
        List<MoveTemplate> formChangeMoves = learnset.getFormChangeMoves();
        Set<MoveTemplate> evolutionMoves = learnset.getEvolutionMoves();

        List<Component> allLines = new ArrayList<>();

        if (!levelUpMoves.isEmpty()) {
            allLines.add(Component.translatable("cobblemon_book_wiki.moves.level_up")
                    .withStyle(ChatFormatting.BOLD));

            levelUpMoves.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        int level = entry.getKey();
                        for (MoveTemplate move : entry.getValue()) {
                            MutableComponent levelComponent = Component.literal("[" + level + "] ")
                                    .withStyle(ChatFormatting.DARK_GRAY);
                            allLines.add(
                                    Component.empty()
                                            .append(levelComponent)
                                            .append(formatMove(move))
                            );
                        }
                    });
            allLines.add(Component.literal(""));
        }

        if (!evolutionMoves.isEmpty()) {
            allLines.add(Component.translatable("cobblemon_book_wiki.moves.evolution")
                    .withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : evolutionMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }

        if (!eggMoves.isEmpty()) {
            allLines.add(Component.translatable("cobblemon_book_wiki.moves.egg")
                    .withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : eggMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }

        if (!tutorMoves.isEmpty()) {
            allLines.add(Component.translatable("cobblemon_book_wiki.moves.tutor")
                    .withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : tutorMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }

        if (!tmMoves.isEmpty()) {
            allLines.add(Component.translatable("cobblemon_book_wiki.moves.tm")
                    .withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : tmMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }

        if (!formChangeMoves.isEmpty()) {
            allLines.add(Component.translatable("cobblemon_book_wiki.moves.form_change")
                    .withStyle(ChatFormatting.BOLD));
            for (MoveTemplate move : formChangeMoves) {
                allLines.add(Component.literal("- ").append(formatMove(move)));
            }
            allLines.add(Component.literal(""));
        }

        List<Component> pages = new ArrayList<>();
        final int LINES_PER_PAGE = 13;

        for (int i = 0; i < allLines.size(); i += LINES_PER_PAGE) {
            int end = Math.min(i + LINES_PER_PAGE, allLines.size());
            MutableComponent page = Component.empty();
            for (Component line : allLines.subList(i, end)) {
                page.append(line).append("\n");
            }
            pages.add(page);
        }

        return pages;
    }

    private static MutableComponent formatMove(MoveTemplate template) {
        MutableComponent hover = Component.translatable("cobblemon_book_wiki.move.info")
                .append("\n");

        ElementalType elementalType = template.getElementalType();

        hover.append(Component.translatable(
                "cobblemon_book_wiki.move.type",
                elementalType.getDisplayName()
        )).append("\n");

        hover.append(Component.translatable(
                "cobblemon_book_wiki.move.category",
                template.getDamageCategory().getDisplayName()
        )).append("\n");

        double powerValue = template.getPower();
        if (powerValue > 0) {
            hover.append(
                    Component.translatable(
                            "cobblemon_book_wiki.move.power",
                            String.format("%.0f", powerValue)
                    )
            ).append(" BP\n");
        }

        double accuracyValue = template.getAccuracy();
        if (accuracyValue >= 0) {
            hover.append(
                    Component.translatable(
                            "cobblemon_book_wiki.move.accuracy",
                            String.format("%.0f", accuracyValue)
                    )
            ).append("%\n");
        }

        hover.append("\n").append(template.getDescription());

        boolean isNormal = elementalType.getName()
                .equals(ElementalTypes.INSTANCE.getNORMAL().getName());

        Style style = Style.EMPTY
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));

        if (!isNormal) {
            style = style.withColor(elementalType.getHue());
        }

        return template.getDisplayName().withStyle(style);
    }
}
