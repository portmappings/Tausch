

package me.portmapping.trading.utils.item;

import me.portmapping.trading.utils.chat.CC;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder implements Cloneable {
    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = this.itemStack.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder setDisplayName(String name) {
        this.itemMeta.setDisplayName(CC.t(name));
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        this.itemMeta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.itemMeta.setLore(CC.t(lore));
        return this;
    }

    public ItemBuilder addToLore(String... entries) {
        List<String> lore = this.itemMeta.hasLore() ? this.itemMeta.getLore() : new ArrayList();
        lore.addAll(Arrays.asList(entries));
        this.itemMeta.setLore(CC.t(lore));
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
        this.itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder storeEnchantment(Enchantment enchantment, int level) {
        if (this.itemMeta instanceof EnchantmentStorageMeta) {
            ((EnchantmentStorageMeta)this.itemMeta).addStoredEnchant(enchantment, level, true);
        }

        return this;
    }

    public ItemBuilder glowing(boolean glowing) {
        if (glowing) {
            this.itemMeta.setEnchantmentGlintOverride(true);
        } else {
            this.itemMeta.setEnchantmentGlintOverride(null);
        }

        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder setUnbreakable(Boolean unbreakable) {
        this.itemMeta.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        if (this.itemMeta instanceof SkullMeta) {
            ((SkullMeta)this.itemMeta).setOwner(owner);
        }

        return this;
    }

    public ItemBuilder setArmorColor(Color color) {
        if (this.itemMeta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta)this.itemMeta).setColor(color);
        }

        return this;
    }

    public ItemBuilder setData(short data) {
        this.itemStack.setDurability(data);
        return this;
    }

    public ItemBuilder setData(int data) {
        this.itemStack.setDurability((short)data);
        return this;
    }

    public ItemBuilder setBookAuthor(String author) {
        if (this.itemMeta instanceof BookMeta) {
            ((BookMeta)this.itemMeta).setAuthor(author);
        }

        return this;
    }

    public ItemBuilder setBookTitle(String title) {
        if (this.itemMeta instanceof BookMeta) {
            ((BookMeta)this.itemMeta).setTitle(title);
        }

        return this;
    }

    public ItemBuilder setBookPages(String... pages) {
        if (this.itemMeta instanceof BookMeta) {
            ((BookMeta)this.itemMeta).setPages(pages);
        }

        return this;
    }

    public ItemBuilder setBookPages(List<String> pages) {
        if (this.itemMeta instanceof BookMeta) {
            ((BookMeta)this.itemMeta).setPages(pages);
        }

        return this;
    }

    public ItemBuilder setPotion(PotionType potion) {
        if (this.itemMeta instanceof PotionMeta) {
            ((PotionMeta)this.itemMeta).setBasePotionType(potion);
        }

        return this;
    }

    public ItemBuilder addFlag(ItemFlag... flags) {
        this.itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack.clone();
    }

    public ItemBuilder clone() {
        return new ItemBuilder(this.build());
    }
}
