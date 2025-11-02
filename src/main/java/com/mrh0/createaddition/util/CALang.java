package com.mrh0.createaddition.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class CALang {

    public static MutableComponent translateDirect(String key, Object... args) {
        return Component.translatable("createaddition." + key, resolveArgs(args));
    }

    public static List<Component> translatedOptions(String prefix, String... keys) {
        List<Component> result = new ArrayList<>(keys.length);
        for (String key : keys)
            result.add(translate((prefix != null ? prefix + "." : "") + key));
        return result;
    }

    public static MutableComponent blockName(BlockState state) {
        return state.getBlock().getName();
    }

    public static MutableComponent itemName(ItemStack stack) {
        return stack.getHoverName().copy();
    }

    // Para FluidStack no Fabric, você precisará adaptar baseado na sua implementação de fluidos
    // Se estiver usando a API de transferência do Fabric, pode ser necessário ajustar
    public static MutableComponent fluidName(net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant fluidVariant) {
        return Component.translatable(fluidVariant.getFluid().getBucket().getDescriptionId());
    }

    public static MutableComponent number(double d) {
        return Component.literal(formatNumber(d));
    }

    public static MutableComponent translate(String langKey, Object... args) {
        return Component.translatable("createaddition." + langKey, resolveArgs(args));
    }

    public static MutableComponent text(String text) {
        return Component.literal(text);
    }

    public static String translateKey(String langKey, Object... args) {
        return "createaddition." + langKey;
    }

    @Deprecated
    public static MutableComponent temporaryText(String text) {
        return Component.literal(text);
    }

    // Métodos auxiliares para substituir a funcionalidade do LangBuilder do Create
    private static Object[] resolveArgs(Object... args) {
        Object[] resolved = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Component) {
                resolved[i] = args[i];
            } else {
                resolved[i] = String.valueOf(args[i]);
            }
        }
        return resolved;
    }

    private static String formatNumber(double d) {
        if (d == (long) d) {
            return String.format("%d", (long) d);
        } else {
            return String.format("%.2f", d);
        }
    }

    // Builder simplificado para compatibilidade com código existente
    public static class Builder {
        private final List<Component> parts = new ArrayList<>();

        public Builder add(Component component) {
            parts.add(component);
            return this;
        }

        public Builder text(String text) {
            parts.add(Component.literal(text));
            return this;
        }

        public Builder translate(String key, Object... args) {
            parts.add(CALang.translate(key, args));
            return this;
        }

        public Component build() {
            if (parts.isEmpty()) {
                return Component.empty();
            }
            if (parts.size() == 1) {
                return parts.get(0);
            }
            MutableComponent result = Component.empty();
            for (Component part : parts) {
                result.append(part);
            }
            return result;
        }

        public MutableComponent component() {
            return (MutableComponent) build();
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}