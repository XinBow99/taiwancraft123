package com.xinbow99.taiwan.entity;

import com.xinbow99.taiwan.Taiwan;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.Vec3;

/**
 * 車款。
 *
 * <h2>為什麼是變體而不是第二個實體種類</h2>
 * <p>兩台車的物理、車主、損壞、落水熄火、引擎聲**完全一樣**——不一樣的只有外型。
 * 開第二個 {@code EntityType} 等於把 {@link Scooter} 那七百行複製一份，而那個檔案還在
 * 調校中：之後每修一個轉向或碰撞的問題都要做兩次，遲早有一次會忘記。
 *
 * <p>而且 {@code ENTITY_TYPE} 是會同步的 registry（見 {@link TaiwanEntities} 的說明），
 * 多一個種類就多一個要對齊的 id。變體存在實體資料裡，不動 registry。
 *
 * <h2>目前只差外觀</h2>
 * <p>性能參數（極速、加速、抓地力）刻意還是共用的。真的要讓 125 跟通用款騎起來不一樣，
 * 這個 enum 就是掛那些數字的地方——但那是另一個決定，不該混在「加一台車」裡面偷偷改掉
 * 現有那台的手感。
 */
public enum ScooterVariant implements StringRepresentable {

    /** 通用速克達。消光深灰、圓頭燈，車身 1.66 格長。 */
    CLASSIC("classic", 0.8f, 1.4f, 0.813f, -0.213f, 0.863f, -0.488f),
    /** 勁戰四代 125。紅黑、體素風的細節，整台放大 1.3 倍後 2.16 格長。 */
    CYGNUS("cygnus", 0.98f, 1.7f, 1.073f, -0.29f, 1.123f, -0.633f);

    private static final ScooterVariant[] BY_ID = values();

    private final String name;
    private final Identifier texture;
    private final EntityDimensions dimensions;
    private final Vec3 riderSeat;
    private final Vec3 pillionSeat;

    ScooterVariant(String name, float width, float height,
                   float riderY, float riderZ, float pillionY, float pillionZ) {
        this.name = name;
        this.texture = Taiwan.id("textures/entity/" + (name.equals("classic") ? "scooter" : name) + ".png");
        this.dimensions = EntityDimensions.scalable(width, height);
        this.riderSeat = new Vec3(0.0, riderY, riderZ);
        this.pillionSeat = new Vec3(0.0, pillionY, pillionZ);
    }

    /**
     * 碰撞箱。
     *
     * <p><b>寬度不能到 1.0</b>：機車相對於汽車的核心賣點就是鑽得過一格的縫隙，
     * 而寬度一旦達到一整格就卡住了。勁戰放大之後車殼是 1.06 格寬，比碰撞箱寬——
     * 那是刻意的，跟船槳一樣只是視覺上超出去。
     */
    public EntityDimensions dimensions() {
        return this.dimensions;
    }

    /**
     * 騎士與後座的座位（實體座標，格）。
     *
     * <p>z 是**負的**：模型的正面是 -Z、算繪時整個轉了 180 度，所以模型往車尾的 +Z
     * 在實體座標裡是 -Z。這個號誌搞反的話，人會坐到踏板前面去。
     *
     * <p>兩台車的數字不一樣，因為坐墊位置本來就不同——共用一組的話，勁戰的騎士會浮在
     * 坐墊上方三分之一格。
     *
     * <p><b>數字是 tools/seat-point.mjs 從 models.js 算出來的，不要手改。</b>坐墊一動就重跑
     * 一次那支腳本。手算要同時處理模型單位換算、地面錨定的縮放、還有上面那個 z 的反向，
     * 三個都對才會落在坐墊上——這種錯只有進遊戲才看得出來，很不划算。
     */
    public Vec3 seat(boolean pillion) {
        return pillion ? this.pillionSeat : this.riderSeat;
    }

    public Identifier texture() {
        return this.texture;
    }

    /**
     * 從同步過來的序號還原。
     *
     * <p>超出範圍就退回 {@link #CLASSIC} 而不是丟例外：這個值會從網路與存檔進來，
     * 舊存檔沒有這個欄位、或是以後刪掉某個車款時都會讀到對不上的數字，
     * 那時候該顯示成一台普通的車，不是讓整個世界載入失敗。
     */
    public static ScooterVariant byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : CLASSIC;
    }

    public static ScooterVariant byName(String name) {
        for (ScooterVariant variant : BY_ID) {
            if (variant.name.equals(name)) return variant;
        }
        return CLASSIC;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
